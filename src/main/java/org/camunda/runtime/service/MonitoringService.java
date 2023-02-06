package org.camunda.runtime.service;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.runtime.facade.dto.dashboard.AuditLog;
import org.camunda.runtime.facade.dto.dashboard.SuccessFailureCount;
import org.camunda.runtime.facade.dto.dashboard.TimeStats;
import org.camunda.runtime.jsonmodel.Connector;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {

  private Long totalExecution = 0L;
  private Map<String, Long> connectorSuccess = new HashMap<>();
  private Map<String, Long> connectorFail = new HashMap<>();
  private Map<String, List<Map<String, Object>>> errors = new HashMap<>();
  private List<AuditLog> auditLogs = new ArrayList<>();
  Map<String, TimeStats> connectorTimeStats = new HashMap<>();

  private Map<String, Long> jobStartTime = new HashMap<>();

  private void increment(Map<String, Long> map, String key) {
    Long count = map.get(key);
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
    long duration =
        System.currentTimeMillis() - jobStartTime.get(connector.getName() + job.getKey());
    jobStartTime.remove(connector.getName() + job.getKey());
    increment(connectorFail, connector.getName());
    if (!errors.containsKey(connector.getName())) {
      errors.put(connector.getName(), new ArrayList<>());
    }
    HashMap<String, Object> error = new HashMap<>();
    error.put("duration", duration);
    error.put("processInstance", job.getProcessInstanceKey());
    error.put("exception", exception);
    error.put("context", job.getVariablesAsMap());
    error.put("connector", connector);
    error.put("date", new Date());
    errors.get(connector.getName()).add(0, error);
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
    auditLogs.add(0, auditLog);
  }

  public List<AuditLog> getAuditLogs() {
    return auditLogs;
  }

  public List<Map<String, Object>> getErrors(String connector) {
    return errors.get(connector);
  }

  public void startJob(Connector connector, ActivatedJob job) {
    jobStartTime.put(connector.getName() + job.getKey(), System.currentTimeMillis());
  }

  public void computeDurationStats(Connector connector, ActivatedJob job) {
    Long end = System.currentTimeMillis();
    Long start = jobStartTime.get(connector.getName() + job.getKey());
    jobStartTime.remove(connector.getName() + job.getKey());
    Long duration = end - start;
    if (!connectorTimeStats.containsKey(connector.getName())) {
      TimeStats ts = new TimeStats();
      ts.setAvg(duration);
      ts.setCount(1L);
      ts.setFastest(duration);
      ts.setSlowest(duration);
      connectorTimeStats.put(connector.getName(), ts);
    } else {
      TimeStats ts = connectorTimeStats.get(connector.getName());
      ts.setAvg((ts.getAvg() * ts.getCount() + duration) / (ts.getCount() + 1));
      ts.setCount(ts.getCount() + 1);
      if (ts.getSlowest() < duration) {
        ts.setSlowest(duration);
      }
      if (ts.getFastest() > duration) {
        ts.setFastest(duration);
      }
      connectorTimeStats.put(connector.getName(), ts);
    }
  }

  public TimeStats getConnectorTimeStats(String connector) {
    return connectorTimeStats.get(connector);
  }
}
