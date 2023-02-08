package org.camunda.runtime.service;

import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.camunda.runtime.MonitoredConnectorJobHandler;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Connector;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class ConnectorExecutionService {

  private ConnectorStorageService connectorStorageService;
  private MonitoringService monitoringService;
  private SecretsService secretsService;
  private ZeebeClientLifecycle zeebeClient;
  private ThreadPoolTaskScheduler scheduler;

  public ConnectorExecutionService(
      ConnectorStorageService connectorStorageService,
      MonitoringService monitoringService,
      SecretsService secretsService,
      ZeebeClientLifecycle zeebeClient,
      ThreadPoolTaskScheduler scheduler) {
    this.zeebeClient = zeebeClient;
    this.secretsService = secretsService;
    this.monitoringService = monitoringService;
    this.connectorStorageService = connectorStorageService;
    this.scheduler = scheduler;
  }

  private Map<String, JobWorker> runningWorkers = new HashMap<>();

  public Connector start(Connector connector) throws TechnicalException {
    File libFile = connectorStorageService.getJarPath(connector).toFile();
    if (!libFile.exists()) {
      throw new TechnicalException("Jar associated to connector couldn't be found");
    }
    try {
      Thread.currentThread().setContextClassLoader(null);
      ClassLoader loader = new URLClassLoader(new URL[] {libFile.toURI().toURL()});

      Class<OutboundConnectorFunction> clazz =
          (Class<OutboundConnectorFunction>) loader.loadClass(connector.getService());

      OutboundConnectorFunction function = clazz.getDeclaredConstructor().newInstance();
      JobWorker worker =
          zeebeClient
              .newWorker()
              .jobType(connector.getJobType())
              .handler(
                  new MonitoredConnectorJobHandler(
                      function, secretsService, connector, monitoringService))
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
    scheduler.schedule(
        new ConnectorStarter(connectors), new Date(System.currentTimeMillis() + 2000));
  }

  private class ConnectorStarter implements Runnable {

    private List<Connector> connectors;

    public ConnectorStarter(List<Connector> connectors) {
      super();
      this.connectors = connectors;
    }

    @Override
    public void run() {
      if (zeebeClient.isRunning()) {
        for (Connector connector : connectors) {
          if (connector.isStarted()) {
            start(connector);
          }
        }

      } else {
        scheduler.schedule(
            new ConnectorStarter(this.connectors), new Date(System.currentTimeMillis() + 2000));
      }
    }
  }
}
