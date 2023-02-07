package org.camunda.runtime.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.camunda.runtime.jsonmodel.Secrets;
import org.camunda.runtime.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSocketUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ClientSocketUtils.class);

  private Socket socket;
  private DataInputStream socketInputStream = null;
  private DataOutputStream socketOutputStream = null;
  private static final Map<String, ClientSocketUtils> INSTANCES = new HashMap<>();

  private ClientSocketUtils(Socket socket) throws IOException {
    this.socket = socket;
    this.socketInputStream = new DataInputStream(socket.getInputStream());
    this.socketOutputStream = new DataOutputStream(socket.getOutputStream());
  }

  public static ClientSocketUtils getInstance(String member) throws IOException {
    if (!INSTANCES.containsKey(member)) {
      Socket socket = getSocket(member);
      ClientSocketUtils instance = new ClientSocketUtils(socket);
      INSTANCES.put(member, instance);
    }
    return INSTANCES.get(member);
  }

  public static void removeInstance(String member) throws IOException {
    INSTANCES.get(member).close();
    INSTANCES.remove(member);
  }

  private void close() throws IOException {
    if (socketInputStream != null) socketInputStream.close();
    if (socketOutputStream != null) socketOutputStream.close();
    if (socket != null) socket.close();
  }

  private static Socket getSocket(String member)
      throws NumberFormatException, UnknownHostException, IOException {
    String[] hostPort = member.split(":");
    return new Socket(hostPort[0], Integer.valueOf(hostPort[1]));
  }

  public boolean healthCheck() {
    try {
      write(SyncService.HEALTHCHECK);
      String message = read();
      if (message.equals(SyncService.HEALTHCHECK_RESPONSE)) {
        return true;
      }
    } catch (IOException e) {
      LOG.error("socket communication exception", e);
    }
    return false;
  }

  private void write(String message) throws IOException {
    socketOutputStream.writeUTF(message);
  }

  private String read() throws IOException {
    return socketInputStream.readUTF();
  }

  public String askSecrets(String string) throws IOException {
    write(SyncService.SHARE_SECRETS + string);
    return read();
  }

  public void shareSecrets(Secrets secrets, byte[] publicKey) throws IOException {
    String secretsJson = JsonUtils.toJson(secrets);
    String encryptedSecrets = CryptoUtils.encrypt(secretsJson, publicKey);
    write(SyncService.UPDATE_SECRETS + encryptedSecrets);
  }
}
