package org.camunda.runtime.jsonmodel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Connector implements Serializable {

  /** Serial version UID */
  private static final long serialVersionUID = -4172075534987185115L;

  private String name;

  private String service;

  private String jobType;

  private boolean started;

  private List<String> fetchVariables;

  private Date modified;

  private String jarFile;

  private String icon;

  public Connector() {}

  public Connector(String name, String jarFile) {
    super();
    this.name = name;
    this.jarFile = jarFile;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  public List<String> getFetchVariables() {
    return fetchVariables;
  }

  public void setFetchVariables(List<String> fetchVariables) {
    this.fetchVariables = fetchVariables;
  }

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public String getJarFile() {
    return jarFile;
  }

  public void setJarFile(String jarFile) {
    this.jarFile = jarFile;
  }

  public boolean isStarted() {
    return started;
  }

  public void setStarted(boolean started) {
    this.started = started;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }
}
