package org.camunda.runtime.facade.dto.dashboard;

import java.io.Serializable;

public class TimeStats implements Serializable {

  /** Serial verison UID */
  private static final long serialVersionUID = -4977613642495472610L;

  private Long avg;
  private Long fastest;
  private Long slowest;
  private Long count;

  public Long getAvg() {
    return avg;
  }

  public void setAvg(Long avg) {
    this.avg = avg;
  }

  public Long getFastest() {
    return fastest;
  }

  public void setFastest(Long fastest) {
    this.fastest = fastest;
  }

  public Long getSlowest() {
    return slowest;
  }

  public void setSlowest(Long slowest) {
    this.slowest = slowest;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }
}
