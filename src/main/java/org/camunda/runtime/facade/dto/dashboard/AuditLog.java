package org.camunda.runtime.facade.dto.dashboard;

import java.io.Serializable;
import java.util.Date;

public class AuditLog implements Comparable<AuditLog>, Serializable {

  /** Serial version UID */
  private static final long serialVersionUID = 956425248212125625L;

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

  @Override
  public int compareTo(AuditLog o) {
    return -1 * this.getDate().compareTo(o.getDate());
  }
}
