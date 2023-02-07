package org.camunda.runtime.facade;

import java.util.List;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.facade.dto.ConnectorError;
import org.camunda.runtime.facade.dto.dashboard.Dashboard;
import org.camunda.runtime.facade.dto.dashboard.TimeStats;
import org.camunda.runtime.security.annotation.IsAuthenticated;
import org.camunda.runtime.service.MonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController extends AbstractController {

  private final Logger LOGGER = LoggerFactory.getLogger(MonitoringController.class);

  @Autowired private MonitoringService monitoringService;

  @IsAuthenticated
  @GetMapping
  @ResponseBody
  public Dashboard dashboard() throws TechnicalException {
    Dashboard dashboard = new Dashboard();
    dashboard.setSuccessFailures(monitoringService.getSuccessFailures());
    dashboard.setTotalExecution(monitoringService.getTotalExecution());
    dashboard.setAuditLogs(monitoringService.getOrderedAuditLogs());
    return dashboard;
  }

  @IsAuthenticated
  @GetMapping("/errors/{connector}")
  public List<ConnectorError> errors(@PathVariable String connector) {
    return monitoringService.getOrderErrors(connector);
  }

  @IsAuthenticated
  @GetMapping("/durations/{connector}")
  public TimeStats timeStats(@PathVariable String connector) {
    return monitoringService.getConnectorTimeStats(connector);
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }
}
