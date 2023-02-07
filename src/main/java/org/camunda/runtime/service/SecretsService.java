package org.camunda.runtime.service;

import io.camunda.connector.api.secret.SecretProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Secrets;
import org.camunda.runtime.utils.CryptoUtils;
import org.camunda.runtime.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretsService implements SecretProvider {
  private static final Logger LOG = LoggerFactory.getLogger(SecretsService.class);

  private Secrets secrets;

  private String status;

  @Value("${workspace:workspace}")
  private String workspace;

  public String getSecret(String key) {
    return secrets.getSecret(key);
  }

  public String getStatus() {
    return status;
  }

  public void setSecret(String key, String value) {
    this.secrets.setSecret(key, value);
    if (this.secrets.isPersistedOnDisk()) {
      persist();
    }
  }

  public void save(Secrets secrets) {
    this.secrets.setPersistedOnDisk(secrets.isPersistedOnDisk());
    this.secrets.setEncrypted(secrets.isEncrypted());
    this.secrets.setPublicKey(secrets.getPublicKey());
    if (secrets.isPersistedOnDisk()) {
      persist();
    } else {
      deleteSecretsFromDisk();
    }
  }

  public Secrets getSecrets() {
    return this.secrets;
  }

  public void setSecrets(Secrets secrets) {
    this.secrets = secrets;
  }

  private Path resolveSecrets() {
    return Path.of(workspace).resolve(ConnectorStorageService.CONNECTORS).resolve("secrets.json");
  }

  private boolean secretsExistsOnDisk() {
    return resolveSecrets().toFile().exists();
  }

  private void persist() throws TechnicalException {
    try {
      if (secrets.isEncrypted()) {
        Secrets encrypted = new Secrets();
        encrypted.setPersistedOnDisk(secrets.isPersistedOnDisk());
        encrypted.setPublicKey(secrets.getPublicKey());
        encrypted.setEncrypted(true);
        for (Map.Entry<String, String> keyValue : secrets.entrySet()) {
          encrypted.setSecret(
              keyValue.getKey(), CryptoUtils.encrypt(keyValue.getValue(), secrets.getPublicKey()));
        }
        JsonUtils.toJsonFile(resolveSecrets(), encrypted);

      } else {
        JsonUtils.toJsonFile(resolveSecrets(), secrets);
      }
    } catch (IOException e) {
      throw new TechnicalException("Error saving the secrets", e);
    }
  }

  public Secrets readFromDisk() throws TechnicalException {
    try {
      return JsonUtils.fromJsonFile(resolveSecrets(), Secrets.class);
    } catch (IOException e) {
      throw new TechnicalException("Error reading the secrets", e);
    }
  }

  private void deleteSecretsFromDisk() {
    File secretFile = resolveSecrets().toFile();
    if (secretFile.exists()) {
      secretFile.delete();
    }
  }

  public void removeSecret(String key) {
    secrets.removeSecret(key);
    if (secrets.isPersistedOnDisk()) {
      persist();
    }
  }

  public byte[] setupKeyPairs(Secrets secrets) {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair pair = generator.generateKeyPair();

      PrivateKey privateKey = pair.getPrivate();
      PublicKey publicKey = pair.getPublic();

      secrets.setPublicKey(publicKey.getEncoded());

      return privateKey.getEncoded();
    } catch (NoSuchAlgorithmException e) {
      throw new TechnicalException("Error while generating encryption keys", e);
    }
  }

  public String restore(byte[] privateKey) {
    String test = "message to validate";
    String encrypted = CryptoUtils.encrypt(test, secrets.getPublicKey());
    String result = CryptoUtils.decrypt(encrypted, privateKey);
    if (!result.equals(test)) {
      return "WARNING : private key is not recognized";
    }
    if (this.secrets.isEncrypted()) {
      for (Map.Entry<String, String> keyValue : secrets.entrySet()) {
        secrets.setSecret(keyValue.getKey(), CryptoUtils.decrypt(keyValue.getValue(), privateKey));
      }
    }
    this.status = "";
    return "SUCCESS : secrets have been decrypted";
  }

  private void createFolders() throws IOException {
    Path wsPath = Path.of(workspace).toAbsolutePath();
    if (!Files.exists(wsPath, LinkOption.NOFOLLOW_LINKS)) {
      Files.createDirectory(wsPath);
    }
    if (!Files.exists(
        wsPath.resolve(ConnectorStorageService.CONNECTORS), LinkOption.NOFOLLOW_LINKS)) {
      Files.createDirectory(wsPath.resolve(ConnectorStorageService.CONNECTORS));
    }
  }

  @PostConstruct
  private void init() throws IOException, TechnicalException {
    createFolders();
    if (secretsExistsOnDisk()) {
      this.secrets = readFromDisk();
      if (this.secrets.isEncrypted()) {
        this.status = "WARNING : secrets need to be decrypted.";
      }
    } else {
      this.secrets = new Secrets();
    }
  }
}
