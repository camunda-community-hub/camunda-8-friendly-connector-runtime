package org.camunda.runtime.facade.dto.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dashboard {

  private Long totalExecution = 0L;

  private Map<String, SuccessFailureCount> successFailures = new HashMap<>();

  private List<AuditLog> auditLogs;

  public Long getTotalExecution() {
    return totalExecution;
  }

  public void setTotalExecution(Long totalExecution) {
    this.totalExecution = totalExecution;
  }

  public Map<String, SuccessFailureCount> getSuccessFailures() {
    return successFailures;
  }

  public void setSuccessFailures(Map<String, SuccessFailureCount> successFailures) {
    this.successFailures = successFailures;
  }

  public List<AuditLog> getAuditLogs() {
    return auditLogs;
  }

  public void setAuditLogs(List<AuditLog> auditLogs) {
    this.auditLogs = auditLogs;
  }
}
