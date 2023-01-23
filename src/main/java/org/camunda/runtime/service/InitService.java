package org.camunda.runtime.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InitService {

  @Value("${workspace:workspace}")
  private String workspace;

  public void createWorkspace() throws IOException {
    Path wsPath = Path.of(workspace).toAbsolutePath();
    if (!Files.exists(wsPath, LinkOption.NOFOLLOW_LINKS)) {
      Files.createDirectory(wsPath);
    }
    if (!Files.exists(
        wsPath.resolve(ConnectorStorageService.CONNECTORS), LinkOption.NOFOLLOW_LINKS)) {
      Files.createDirectory(wsPath.resolve(ConnectorStorageService.CONNECTORS));
    }
    if (!Files.exists(
        wsPath.resolve(ConnectorStorageService.CONNECTORS).resolve(ConnectorStorageService.JARS),
        LinkOption.NOFOLLOW_LINKS)) {
      Files.createDirectory(
          wsPath.resolve(ConnectorStorageService.CONNECTORS).resolve(ConnectorStorageService.JARS));
    }
  }

  @PostConstruct
  private void init() throws IOException {
    createWorkspace();
  }
}
