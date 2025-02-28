package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models;

import java.sql.Timestamp;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReportReviewRequestResponse extends Report implements Response {
  private String reportReviewRequestId;

  private java.sql.Timestamp reportReviewRequestCreationDate;

  private java.sql.Timestamp reportReviewRequestClosedDate;

  private UserLdap hasReviewer;

  private ReviewReport requestedReview;

  public ReportReviewRequestResponse (
    String reportReviewRequestId,
    Timestamp reportReviewRequestCreationDate,
    Timestamp reportReviewRequestClosedDate,
    String reviewer,
    UUID reportDataCatalogId
  ) {
    this.reportReviewRequestId = reportReviewRequestId;
    this.reportReviewRequestCreationDate = reportReviewRequestCreationDate;
    this.reportReviewRequestClosedDate = reportReviewRequestClosedDate;

    if (StringUtils.isNotEmpty(reviewer)) {
      hasReviewer = new UserLdap(reviewer);
    }

    if (reportDataCatalogId != null) {
      requestedReview = new ReviewReport(reportDataCatalogId);
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class ReviewReport {
    private UUID reportDataCatalogId;
  }
}
