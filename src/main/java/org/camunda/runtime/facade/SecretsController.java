package org.camunda.runtime.facade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.runtime.facade.dto.Secret;
import org.camunda.runtime.jsonmodel.Secrets;
import org.camunda.runtime.security.annotation.IsAdmin;
import org.camunda.runtime.security.annotation.IsAuthenticated;
import org.camunda.runtime.service.SecretsService;
import org.camunda.runtime.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/secrets")
public class SecretsController extends AbstractController {

  private final Logger logger = LoggerFactory.getLogger(SecretsController.class);

  @Autowired private SecretsService secretsService;
  @Autowired private SyncService syncService;

  Map<String, byte[]> privateKeyMap = new HashMap<>();

  @IsAuthenticated
  @GetMapping
  public Secrets get() {
    Secrets anonymized = new Secrets();
    anonymized.setPersistedOnDisk(secretsService.getSecrets().isPersistedOnDisk());
    anonymized.setEncrypted(secretsService.getSecrets().isEncrypted());
    for (Map.Entry<String, String> keyValue : secretsService.getSecrets().entrySet()) {
      anonymized.setSecret(keyValue.getKey(), "*".repeat(keyValue.getValue().length()));
    }
    return anonymized;
  }

  @IsAdmin
  @PostMapping("/add")
  public void setSecret(@RequestBody Secret secret) {
    secretsService.setSecret(secret.getKey(), secret.getValue());
    syncService.shareSecretWithOtherMembers(secretsService.getSecrets());
  }

  @IsAdmin
  @DeleteMapping("/{key}")
  public void deleteSecret(@PathVariable String key) {
    secretsService.removeSecret(key);
    syncService.shareSecretWithOtherMembers(secretsService.getSecrets());
  }

  @IsAdmin
  @PostMapping
  @ResponseBody
  public Map<String, byte[]> save(@RequestBody Secrets secrets) {
    if (secrets.isEncrypted()) {
      String key = UUID.randomUUID().toString();
      byte[] privateKey = secretsService.setupKeyPairs(secrets);
      secretsService.save(secrets);
      privateKeyMap.put(key, privateKey);

      syncService.shareSecretWithOtherMembers(secretsService.getSecrets());
      return Map.of("privateKey", privateKey);
    }
    secretsService.save(secrets);
    privateKeyMap.clear();

    syncService.shareSecretWithOtherMembers(secretsService.getSecrets());
    return Map.of("privateKey", new byte[] {});
  }

  @IsAuthenticated
  @GetMapping("/status")
  public String status() {
    return secretsService.getStatus();
  }

  @IsAdmin
  @PostMapping("/restore")
  public String restore(@RequestBody Map<String, byte[]> privateKey) {
    return secretsService.restore(privateKey.get("privateKey"));
  }

  @Override
  public Logger getLogger() {
    return logger;
  }
}
