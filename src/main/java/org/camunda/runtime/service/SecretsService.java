package org.camunda.runtime.service;

import io.camunda.connector.api.secret.SecretProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Secrets;
import org.camunda.runtime.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretsService implements SecretProvider {

  public Secrets secrets;

  @Value("${workspace:workspace}")
  private String workspace;

  public String getSecret(String key) {
    return secrets.getSecret(key);
  }

  public void setSecret(String key, String value) {
    secrets.setSecret(key, value);
    if (secrets.isPersistedOnDisk()) {
      persist();
    }
  }

  public void save(Secrets secrets) {
    this.secrets.setPersistedOnDisk(secrets.isPersistedOnDisk());
    if (secrets.isPersistedOnDisk()) {
      persist();
    } else {
      deleteSecretsFromDisk();
    }
  }

  public Secrets getSecrets() {
    return this.secrets;
  }

  private Path resolveSecrets() {
    return Path.of(workspace).resolve(ConnectorStorageService.CONNECTORS).resolve("secrets.json");
  }

  private boolean secretsExistsOnDisk() {
    return resolveSecrets().toFile().exists();
  }

  private void persist() throws TechnicalException {
    try {
      JsonUtils.toJsonFile(resolveSecrets(), secrets);
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

  @PostConstruct
  private void init() throws TechnicalException {
    if (secretsExistsOnDisk()) {
      this.secrets = readFromDisk();
    } else {
      this.secrets = new Secrets();
    }
  }

  public void removeSecret(String key) {
    secrets.removeSecret(key);
    if (secrets.isPersistedOnDisk()) {
      persist();
    }
  }
}
