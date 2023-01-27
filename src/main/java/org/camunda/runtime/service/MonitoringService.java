package org.camunda.runtime.service;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.runtime.facade.dto.dashboard.AuditLog;
import org.camunda.runtime.facade.dto.dashboard.SuccessFailureCount;
import org.camunda.runtime.jsonmodel.Connector;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {

  private Long totalExecution = 0L;
  private Map<String, Long> connectorSuccess = new HashMap<>();
  private Map<String, Long> connectorFail = new HashMap<>();
  private Map<String, List<Map<String, Object>>> errors = new HashMap<>();
  private List<AuditLog> auditLogs = new ArrayList<>();

  private void increment(Map<String, Long> map, String key) {
    Long count = connectorSuccess.get(key);
    if (count == null) {
      map.put(key, 1L);
    } else {
      map.put(key, count + 1);
    }
  }

  public void addSuccess(Connector connector) {
    totalExecution++;
    increment(connectorSuccess, connector.getName());
  }

  public void addFailure(Connector connector, ActivatedJob job, String exception) {
    totalExecution++;
    increment(connectorFail, connector.getName());
    if (!errors.containsKey(connector.getName())) {
      errors.put(connector.getName(), new ArrayList<>());
    }
    HashMap<String, Object> error = new HashMap<>();
    error.put("processInstance", job.getProcessInstanceKey());
    error.put("exception", exception);
    error.put("context", job.getVariablesAsMap());
    error.put("connector", connector);
    errors.get(connector.getName()).add(error);
  }

  public Map<String, SuccessFailureCount> getSuccessFailures() {
    Map<String, SuccessFailureCount> result = new HashMap<>();
    Set<String> connectors = new HashSet<>();
    connectors.addAll(connectorSuccess.keySet());
    connectors.addAll(connectorFail.keySet());
    for (String connector : connectors) {
      Long success = connectorSuccess.get(connector);
      success = success != null ? success : 0L;
      Long fail = connectorFail.get(connector);
      fail = fail != null ? fail : 0L;
      result.put(connector, new SuccessFailureCount(success, fail));
    }
    return result;
  }

  public Long getTotalExecution() {
    return totalExecution;
  }

  public void addAuditLog(AuditLog auditLog) {
    auditLogs.add(auditLog);
  }

  public List<AuditLog> getAuditLogs() {
    return auditLogs;
  }

  public List<Map<String, Object>> getErrors(String connector) {
    return errors.get(connector);
  }
}
