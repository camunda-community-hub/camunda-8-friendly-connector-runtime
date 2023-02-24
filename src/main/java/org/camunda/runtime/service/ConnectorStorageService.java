package org.camunda.runtime.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import io.camunda.connector.api.annotation.OutboundConnector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Connector;
import org.camunda.runtime.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ConnectorStorageService {

  public static final String CONNECTORS = "connectors";
  public static final String JARS = "jars";

  @Value("${workspace:workspace}")
  private String workspace;

  public Path resolve(String name) {
    return Path.of(workspace).resolve(CONNECTORS).resolve(name).resolve("connector.json");
  }

  public Path resolveEltTemplate(String name) {
    return Path.of(workspace).resolve(CONNECTORS).resolve(name).resolve("element-template.json");
  }

  public List<Connector> all() throws TechnicalException {
    File[] connectorFiles = Path.of(workspace).resolve(CONNECTORS).toFile().listFiles();
    List<Connector> connectors = new ArrayList<>();
    for (File file : connectorFiles) {
      if (!file.getName().equals(JARS) && !file.getName().equals("secrets.json")) {
        Connector connector = findByName(file.getName());
        try {
          JsonNode template = getEltTemplate(file.getName());
          if (template.get("icon") != null && template.get("icon").get("contents") != null) {
            connector.setIcon(template.get("icon").get("contents").asText());
          }
        } catch (TechnicalException e) {

        }
        connectors.add(connector);
      }
    }
    return connectors;
  }

  public Connector findByName(String connectorName) throws TechnicalException {
    try {
      return JsonUtils.fromJsonFile(resolve(connectorName), Connector.class);
    } catch (IOException e) {
      throw new TechnicalException("Error reading the connector " + connectorName, e);
    }
  }

  public Connector save(Connector connector) throws TechnicalException {
    try {
      connector.setModified(new Date());
      JsonUtils.toJsonFile(resolve(connector.getName()), connector);
      return connector;
    } catch (IOException e) {
      throw new TechnicalException("Error saving the connector " + connector.getName(), e);
    }
  }

  public File storeJarFile(MultipartFile file) throws TechnicalException {
    try {
      return storeJarFile(file.getOriginalFilename(), file.getInputStream());
    } catch (IOException e) {
      throw new TechnicalException("Error storing the library", e);
    }
  }

  public File storeJarFile(String name, InputStream content) throws TechnicalException {
    try {
      File target = Path.of(workspace).resolve(CONNECTORS).resolve(JARS).resolve(name).toFile();
      if (!target.exists()) {
        target.createNewFile();
      }
      try (FileOutputStream out = new FileOutputStream(target)) {
        IOUtils.copy(content, out);
      }
      return target;
    } catch (IOException e) {
      throw new TechnicalException("Error storing the library", e);
    }
  }

  public Path getJarPath(Connector connector) {
    return Path.of(workspace).resolve(CONNECTORS).resolve(JARS).resolve(connector.getJarFile());
  }

  public void deleteByName(String name) throws TechnicalException {
    try {
      FileUtils.deleteDirectory(Path.of(workspace).resolve(CONNECTORS).resolve(name).toFile());
    } catch (IOException e) {
      throw new TechnicalException("Error storing the library", e);
    }
  }

  @PostConstruct
  private void createFolders() throws IOException {
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

  public void fetchDetails(Connector connector) throws TechnicalException {
    File libFile = getJarPath(connector).toFile();
    if (!libFile.exists()) {
      throw new TechnicalException("Jar associated to connector couldn't be found");
    }
    try {
      ZipFile jarFile = new ZipFile(libFile);
      Enumeration<? extends ZipEntry> entries = jarFile.entries();

      URLClassLoader loader = new URLClassLoader(new URL[] {libFile.toURI().toURL()});

      while (connector.getService() == null && entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String entryName = entry.getName();
        if (entryName != null && entryName.endsWith(".class")) {
          String className = entryName.replace(".class", "").replace('/', '.');
          Class<?> clazz = (Class<?>) loader.loadClass(className);
          OutboundConnector connectorAnnotation = clazz.getAnnotation(OutboundConnector.class);
          if (connectorAnnotation != null) {
            connector.setService(className);
            connector.setFetchVariables(Lists.newArrayList(connectorAnnotation.inputVariables()));
            connector.setName(connectorAnnotation.name());
            connector.setJobType(connectorAnnotation.type());
          }
        }
      }
      jarFile.close();
      loader.close();
    } catch (IOException | ClassNotFoundException e) {

    }
  }

  public void saveElementTemplate(Connector connector, JsonNode elementTemplateTree)
      throws TechnicalException {
    try {
      JsonUtils.toJsonFile(resolveEltTemplate(connector.getName()), elementTemplateTree);
    } catch (IOException e) {
      throw new TechnicalException("Error saving the element template " + connector.getName(), e);
    }
  }

  public JsonNode getEltTemplate(String name) throws TechnicalException {
    try {
      return JsonUtils.fromJsonFile(resolveEltTemplate(name), JsonNode.class);
    } catch (IOException e) {
      throw new TechnicalException("Error reading the element template " + name, e);
    }
  }
}
