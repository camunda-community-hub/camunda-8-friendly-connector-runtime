package org.camunda.runtime.facade.dto;

public class OutOfTheBoxConnector {
  private String name;
  private String release;

  public OutOfTheBoxConnector() {}

  public OutOfTheBoxConnector(String name, String release) {
    this.name = name;
    this.release = release;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRelease() {
    return release;
  }

  public void setRelease(String release) {
    this.release = release;
  }
}
