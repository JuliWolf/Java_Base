package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions;

/**
 * @author juliwolf
 */

public class ReportReviewRequestNotFoundException extends RuntimeException {
  public ReportReviewRequestNotFoundException () {
    super("Report review request not found");
  }
}
