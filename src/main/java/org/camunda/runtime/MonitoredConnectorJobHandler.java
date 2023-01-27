package org.camunda.runtime;

import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.api.secret.SecretProvider;
import io.camunda.connector.api.validation.ValidationProvider;
import io.camunda.connector.runtime.util.outbound.ConnectorJobHandler;
import io.camunda.connector.runtime.util.outbound.ConnectorResult;
import io.camunda.connector.validation.impl.DefaultValidationProvider;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.camunda.runtime.jsonmodel.Connector;
import org.camunda.runtime.service.MonitoringService;

public class MonitoredConnectorJobHandler extends ConnectorJobHandler {

  private Connector connector;
  private MonitoringService monitoringService;

  public MonitoredConnectorJobHandler(
      final OutboundConnectorFunction call,
      final SecretProvider secretProvider,
      final Connector connector,
      final MonitoringService monitoringService) {
    super(call, secretProvider);
    this.connector = connector;
    this.monitoringService = monitoringService;
  }

  protected void completeJob(JobClient client, ActivatedJob job, ConnectorResult result) {
    client.newCompleteCommand(job).variables(result.getVariables()).send().join();
    monitoringService.addSuccess(connector);
  }

  protected void failJob(JobClient client, ActivatedJob job, Exception exception) {
    client.newFailCommand(job).retries(0).errorMessage(exception.getMessage()).send().join();
    monitoringService.addFailure(connector, job, exception.getMessage());
  }

  public ValidationProvider getValidationProvier() {
    return new DefaultValidationProvider();
  }
}
