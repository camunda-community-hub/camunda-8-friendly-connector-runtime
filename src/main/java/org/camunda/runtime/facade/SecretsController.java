package org.camunda.runtime.facade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.runtime.facade.dto.Secret;
import org.camunda.runtime.jsonmodel.Secrets;
import org.camunda.runtime.security.annotation.IsAdmin;
import org.camunda.runtime.security.annotation.IsAuthenticated;
import org.camunda.runtime.service.SecretsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
  }

  @IsAdmin
  @DeleteMapping("/delete/{key}")
  public void deleteSecret(@PathVariable String key) {
    secretsService.removeSecret(key);
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
      return Map.of("privateKey", privateKey);
    }
    secretsService.save(secrets);
    privateKeyMap.clear();
    return Map.of("privateKey", null);
  }

  @GetMapping("/{key}/privateKey.key")
  public ResponseEntity<Resource> downloadPrivateKey(@PathVariable String key) {
    byte[] privateKey = privateKeyMap.get(key);
    HttpHeaders header = new HttpHeaders();
    header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=privateKey.key");
    header.add("Cache-Control", "no-cache, no-store, must-revalidate");
    header.add("Pragma", "no-cache");
    header.add("Expires", "0");

    ByteArrayResource resource = new ByteArrayResource(privateKey);
    privateKeyMap.clear();
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(privateKey.length)
        .body(resource);
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
