package org.camunda.runtime.jsonmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Secrets {

  private boolean persistedOnDisk;
  private boolean encrypted;
  private byte[] publicKey;
  private Map<String, String> secretsKeyValues = new HashMap<>();

  public boolean isPersistedOnDisk() {
    return persistedOnDisk;
  }

  public void setPersistedOnDisk(boolean persistedOnDisk) {
    this.persistedOnDisk = persistedOnDisk;
  }

  public boolean isEncrypted() {
    return encrypted;
  }

  public void setEncrypted(boolean encrypted) {
    this.encrypted = encrypted;
  }

  public byte[] getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(byte[] publicKey) {
    this.publicKey = publicKey;
  }

  public Map<String, String> getSecretsKeyValues() {
    return secretsKeyValues;
  }

  public void setSecretsKeyValues(Map<String, String> secretsKeyValues) {
    this.secretsKeyValues = secretsKeyValues;
  }

  public String getSecret(String key) {
    return secretsKeyValues.get(key);
  }

  public void setSecret(String key, String value) {
    secretsKeyValues.put(key, value);
  }

  public void removeSecret(String key) {
    secretsKeyValues.remove(key);
  }

  public Set<Map.Entry<String, String>> entrySet() {
    return secretsKeyValues.entrySet();
  }
}
