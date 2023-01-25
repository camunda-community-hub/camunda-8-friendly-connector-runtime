package org.camunda.runtime.jsonmodel;

import java.util.Date;
import java.util.Map;

public class Translation {

  private String name;

  private String code;

  private Date modified;

  private Map<String, String> siteTranslations;

  public Translation() {}

  public Translation(String name, String code) {
    super();
    this.name = name;
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public Map<String, String> getSiteTranslations() {
    return siteTranslations;
  }

  public void setSiteTranslations(Map<String, String> siteTranslations) {
    this.siteTranslations = siteTranslations;
  }
}
