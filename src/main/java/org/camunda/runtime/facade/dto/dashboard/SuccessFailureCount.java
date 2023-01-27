package org.camunda.runtime.facade.dto.dashboard;

public class SuccessFailureCount {

  private Long success;
  private Long failure;

  public SuccessFailureCount() {}

  public SuccessFailureCount(Long success, Long failure) {
    this.success = success;
    this.failure = failure;
  }

  public Long getSuccess() {
    return success;
  }

  public void setSuccess(Long success) {
    this.success = success;
  }

  public Long getFailure() {
    return failure;
  }

  public void setFailure(Long failure) {
    this.failure = failure;
  }
}
