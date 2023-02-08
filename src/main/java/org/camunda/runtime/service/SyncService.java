package org.camunda.runtime.service;

import com.hazelcast.core.HazelcastInstance;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Secrets;
import org.camunda.runtime.utils.ClientSocketUtils;
import org.camunda.runtime.utils.CryptoUtils;
import org.camunda.runtime.utils.JsonUtils;
import org.camunda.runtime.utils.ServerSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service used to sync if multiple instances are started */
@Service
public class SyncService {
  private static final Logger LOG = LoggerFactory.getLogger(SyncService.class);
  public static final String HEALTHCHECK = "Howdy";
  public static final String HEALTHCHECK_RESPONSE = "Hi there";
  public static final String SHARE_SECRETS = "sharesecrets:";
  public static final String UPDATE_SECRETS = "updatesecrets:";

  @Value("${server.socketPort:5000}")
  private Integer port;

  @Autowired private SecretsService secretsService;
  @Autowired private HazelcastInstance hazelcastInstance;

  private String ip;
  private PrivateKey privateKey;
  private PublicKey publicKey;

  private void getMyIp() {
    try (final DatagramSocket socket = new DatagramSocket()) {
      socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
      this.ip = socket.getLocalAddress().getHostAddress();
    } catch (UnknownHostException | SocketException e) {
      this.ip = "ip unknown";
    }
  }

  private void setupKeyPairs() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair pair = generator.generateKeyPair();

      this.privateKey = pair.getPrivate();
      this.publicKey = pair.getPublic();
    } catch (NoSuchAlgorithmException e) {
      throw new TechnicalException("Error while generating encryption keys", e);
    }
  }

  private void register() throws IOException {
    getMyIp();
    setupKeyPairs();
    System.out.println(this.ip);
    getMembers().put(this.ip + ":" + port, publicKey.getEncoded());
    ServerSocketUtils.getInstance(port, secretsService, this).startListening();
  }

  private Map<String, byte[]> getMembers() {
    return hazelcastInstance.getMap("members");
  }

  public byte[] getMember(String member) {
    return getMembers().get(member);
  }

  private void removeMember(String member) throws IOException {
    getMembers().remove(member);
    ClientSocketUtils.removeInstance(member);
  }

  private void contactOtherMembers() throws IOException {
    String[] members = getMembers().keySet().toArray(new String[0]);
    for (String member : members) {
      if (!member.equals(this.ip + ":" + port)) {
        boolean available = checkMember(member);
        if (!available) {
          removeMember(member);
        }
      }
    }
  }

  private void askSecretsAndUpdateMe() {
    for (Map.Entry<String, byte[]> member : getMembers().entrySet()) {
      if (!member.getKey().equals(this.ip + ":" + port)) {
        try {
          String encrypted =
              ClientSocketUtils.getInstance(member.getKey()).askSecrets(this.ip + ":" + this.port);
          String decrypted = CryptoUtils.decrypt(encrypted, this.privateKey.getEncoded());
          Secrets secrets = JsonUtils.toObject(decrypted, Secrets.class);
          secretsService.setSecrets(secrets);
          break;
        } catch (TechnicalException | IOException e) {
          LOG.error("Error asking secrets from " + member.getKey(), e);
        }
      }
    }
  }

  private boolean checkMember(String member) throws IOException {
    boolean available = ClientSocketUtils.getInstance(member).healthCheck();
    int i = 1;
    while (!available && i < 4) {
      try {
        Thread.sleep(i * 500);
      } catch (InterruptedException e) {
      }
      available = ClientSocketUtils.getInstance(member).healthCheck();
      i++;
    }
    return available;
  }

  public void shareSecretWithOtherMembers(Secrets secrets) {
    for (Map.Entry<String, byte[]> member : getMembers().entrySet()) {
      if (!member.getKey().equals(this.ip + ":" + port)) {
        try {
          ClientSocketUtils.getInstance(member.getKey()).shareSecrets(secrets, member.getValue());
        } catch (IOException e) {
          LOG.error("Error sharing secret updates with " + member.getKey(), e);
          try {
            boolean available = checkMember(member.getKey());
            if (!available) {
              LOG.error(member.getKey() + " is unreachable and will be removed");
              removeMember(member.getKey());
            }
          } catch (IOException e2) {
            LOG.error(member.getKey() + " can't be verified", e2);
          }
        }
      }
    }
  }

  public void updateSecrets(String encryptedSecrets) {
    String decryptedSecrets = CryptoUtils.decrypt(encryptedSecrets, privateKey.getEncoded());
    Secrets secrets = JsonUtils.toObject(decryptedSecrets, Secrets.class);
    secretsService.setSecrets(secrets);
  }

  @PostConstruct
  public void init() throws IOException {
    register();
    contactOtherMembers();
    askSecretsAndUpdateMe();
  }
}
