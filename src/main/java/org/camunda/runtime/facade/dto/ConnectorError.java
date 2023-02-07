package org.camunda.runtime.facade.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import org.camunda.runtime.jsonmodel.Connector;

public class ConnectorError implements Serializable, Comparable<ConnectorError> {

  /** serial version UID */
  private static final long serialVersionUID = 2900207801012427305L;

  private Long duration;
  private Long processInstance;
  private String exception;
  private Map<String, Object> context;
  private Connector connector;
  private Date date;

  public ConnectorError() {
    super();
    this.date = new Date();
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public Long getProcessInstance() {
    return processInstance;
  }

  public void setProcessInstance(Long processInstance) {
    this.processInstance = processInstance;
  }

  public String getException() {
    return exception;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  public Connector getConnector() {
    return connector;
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  @Override
  public int compareTo(ConnectorError o) {
    return -1 * this.getDate().compareTo(o.getDate());
  }
}
