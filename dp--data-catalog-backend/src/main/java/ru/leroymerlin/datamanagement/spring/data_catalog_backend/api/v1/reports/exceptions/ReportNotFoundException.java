package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions;

/**
 * @author juliwolf
 */

public class ReportNotFoundException extends RuntimeException {
  public ReportNotFoundException () {
    super("Requested report not found");
  }
}
