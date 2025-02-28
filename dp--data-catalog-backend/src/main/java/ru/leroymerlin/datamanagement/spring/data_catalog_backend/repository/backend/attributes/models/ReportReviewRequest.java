package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models;

import java.util.UUID;

/**
 * @author juliwolf
 */

public interface ReportReviewRequest {
  String getReportReviewRequestId();

  java.sql.Timestamp getReportReviewRequestCreationDate();

  java.sql.Timestamp getReportReviewRequestClosedDate();

  UUID getReportDataCatalogId();

  String getLdap();
}
