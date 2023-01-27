package org.camunda.runtime.facade.dto.dashboard;

import java.util.Date;

public class AuditLog {

  private String action;
  private String connector;
  private String author;
  private Date date;

  public AuditLog() {}

  public AuditLog(String action, String connector, String author) {
    this.action = action;
    this.connector = connector;
    this.author = author;
    this.date = new Date();
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getConnector() {
    return connector;
  }

  public void setConnector(String connector) {
    this.connector = connector;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
