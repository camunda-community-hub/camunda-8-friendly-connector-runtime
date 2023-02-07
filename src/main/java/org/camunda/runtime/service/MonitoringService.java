package org.camunda.runtime.service;

import com.hazelcast.core.HazelcastInstance;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.runtime.facade.dto.ConnectorError;
import org.camunda.runtime.facade.dto.dashboard.AuditLog;
import org.camunda.runtime.facade.dto.dashboard.SuccessFailureCount;
import org.camunda.runtime.facade.dto.dashboard.TimeStats;
import org.camunda.runtime.jsonmodel.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {

  @Autowired private HazelcastInstance hazelcastInstance;

  private Map<String, Long> jobStartTime = new HashMap<>();

  private Map<String, Long> getGlobal() {
    return hazelcastInstance.getMap("global");
  }

  private Map<String, Long> getConnectorSuccess() {
    return hazelcastInstance.getMap("connectorSuccess");
  }

  private Map<String, Long> getConnectorFail() {
    return hazelcastInstance.getMap("connectorFail");
  }

  private List<ConnectorError> getErrors(String connector) {
    return hazelcastInstance.getList(connector + "Errors");
  }

  private List<AuditLog> getAuditLogs() {
    return hazelcastInstance.getList("auditLogs");
  }

  private Map<String, TimeStats> getTimeStats() {
    return hazelcastInstance.getMap("connectorTimeStats");
  }

  private void increment(Map<String, Long> map, String key) {
    Long count = map.get(key);
    if (count == null) {
      map.put(key, 1L);
    } else {
      map.put(key, count + 1);
    }
  }

  public synchronized void incrementExecutions() {
    Long exec = getGlobal().get("totalExecution");
    exec = exec == null ? 0 : exec;
    getGlobal().put("totalExecution", exec + 1);
  }

  public void addSuccess(Connector connector) {
    incrementExecutions();
    increment(getConnectorSuccess(), connector.getName());
  }

  public void addFailure(Connector connector, ActivatedJob job, String exception) {
    incrementExecutions();
    long duration =
        System.currentTimeMillis() - jobStartTime.get(connector.getName() + job.getKey());
    jobStartTime.remove(connector.getName() + job.getKey());
    increment(getConnectorFail(), connector.getName());

    ConnectorError error = new ConnectorError();
    error.setDuration(duration);
    error.setProcessInstance(job.getProcessInstanceKey());
    error.setException(exception);
    error.setContext(job.getVariablesAsMap());
    error.setConnector(connector);
    getErrors(connector.getName()).add(error);
  }

  public Map<String, SuccessFailureCount> getSuccessFailures() {
    Map<String, SuccessFailureCount> result = new HashMap<>();
    Set<String> connectors = new HashSet<>();
    connectors.addAll(getConnectorSuccess().keySet());
    connectors.addAll(getConnectorFail().keySet());
    for (String connector : connectors) {
      Long success = getConnectorSuccess().get(connector);
      success = success != null ? success : 0L;
      Long fail = getConnectorFail().get(connector);
      fail = fail != null ? fail : 0L;
      result.put(connector, new SuccessFailureCount(success, fail));
    }
    return result;
  }

  public Long getTotalExecution() {
    return getGlobal().get("totalExecution");
  }

  public void addAuditLog(AuditLog auditLog) {
    getAuditLogs().add(0, auditLog);
  }

  public List<AuditLog> getOrderedAuditLogs() {
    List<AuditLog> auditLogs = new ArrayList<>();
    auditLogs.addAll(getAuditLogs());
    Collections.sort(auditLogs);
    return auditLogs;
  }

  public List<ConnectorError> getOrderErrors(String connector) {
    List<ConnectorError> errors = new ArrayList<>();
    errors.addAll(getErrors(connector));
    Collections.sort(errors);
    return errors;
  }

  public void startJob(Connector connector, ActivatedJob job) {
    jobStartTime.put(connector.getName() + job.getKey(), System.currentTimeMillis());
  }

  public void computeDurationStats(Connector connector, ActivatedJob job) {
    Long end = System.currentTimeMillis();
    Long start = jobStartTime.get(connector.getName() + job.getKey());
    jobStartTime.remove(connector.getName() + job.getKey());
    Long duration = end - start;
    if (!getTimeStats().containsKey(connector.getName())) {
      TimeStats ts = new TimeStats();
      ts.setAvg(duration);
      ts.setCount(1L);
      ts.setFastest(duration);
      ts.setSlowest(duration);
      getTimeStats().put(connector.getName(), ts);
    } else {
      TimeStats ts = getTimeStats().get(connector.getName());
      ts.setAvg((ts.getAvg() * ts.getCount() + duration) / (ts.getCount() + 1));
      ts.setCount(ts.getCount() + 1);
      if (ts.getSlowest() < duration) {
        ts.setSlowest(duration);
      }
      if (ts.getFastest() > duration) {
        ts.setFastest(duration);
      }
      getTimeStats().put(connector.getName(), ts);
    }
  }

  public TimeStats getConnectorTimeStats(String connector) {
    return getTimeStats().get(connector);
  }
}
