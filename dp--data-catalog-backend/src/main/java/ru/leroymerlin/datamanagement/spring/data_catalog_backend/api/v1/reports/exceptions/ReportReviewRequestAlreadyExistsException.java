package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions;

/**
 * @author juliwolf
 */

public class ReportReviewRequestAlreadyExistsException extends RuntimeException {
  public ReportReviewRequestAlreadyExistsException () {
    super("Report review request already exists for this report");
  }
}
