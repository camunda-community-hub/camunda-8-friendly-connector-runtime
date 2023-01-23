package org.camunda.runtime.service;

import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.runtime.util.outbound.ConnectorJobHandler;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.PostConstruct;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConnectorExecutionService {

  @Autowired private ConnectorStorageService connectorStorageService;
  @Autowired private SecretsService secretsService;
  @Autowired private ZeebeClient zeebeClient;

  private Map<String, JobWorker> runningWorkers = new HashMap<>();

  public Connector start(Connector connector) throws TechnicalException {
    File libFile = connectorStorageService.getJarPath(connector).toFile();
    if (!libFile.exists()) {
      throw new TechnicalException("Jar associated to connector couldn't be found");
    }
    try {
      ZipFile jarFile = new ZipFile(libFile);
      ZipEntry service =
          jarFile.getEntry(
              "META-INF/services/io.camunda.connector.api.outbound.OutboundConnectorFunction");
      InputStream serviceStream = jarFile.getInputStream(service);
      String functionName = new String(serviceStream.readAllBytes(), StandardCharsets.UTF_8);

      ClassLoader loader = URLClassLoader.newInstance(new URL[] {libFile.toURI().toURL()});

      Class<OutboundConnectorFunction> clazz =
          (Class<OutboundConnectorFunction>) loader.loadClass(functionName.trim());

      OutboundConnectorFunction function = clazz.getDeclaredConstructor().newInstance();
      JobWorker worker =
          zeebeClient
              .newWorker()
              .jobType(connector.getJobType())
              .handler(new ConnectorJobHandler(function, secretsService))
              .name(connector.getName())
              .fetchVariables(connector.getFetchVariables())
              .open();
      connector.setStarted(true);
      addRunningWorker(connector, worker);
      return connector;
    } catch (IOException
        | ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new TechnicalException("Error executing the connector library");
    }
  }

  public Connector stop(Connector connector) {
    JobWorker worker = runningWorkers.get(connector.getJobType());
    worker.close();
    runningWorkers.remove(connector.getJobType());
    connector.setStarted(false);
    return connector;
  }

  private void addRunningWorker(Connector connector, JobWorker worker) {
    runningWorkers.put(connector.getJobType(), worker);
  }

  @PostConstruct
  private void init() throws TechnicalException {
    // restart connectors that were running before the application shutdown
    List<Connector> connectors = connectorStorageService.all();
    for (Connector connector : connectors) {
      if (connector.isStarted()) {
        start(connector);
      }
    }
  }
}
