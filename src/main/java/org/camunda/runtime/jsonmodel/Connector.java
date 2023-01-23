package org.camunda.runtime.jsonmodel;

import java.util.Date;
import java.util.List;

public class Connector {

  private String name;

  private String jobType;

  private boolean started;

  private List<String> fetchVariables;

  private Date modified;

  private String jarFile;

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
}
