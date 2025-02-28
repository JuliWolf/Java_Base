package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models;

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
public class ReportResponse extends Report implements Response {
  private UUID reportId;

  private String reportName;

  private UUID reportDataCatalogId;

  private String reportType;

  private String reportDescription;

  private String reportLink;

  private String reportTemplate;

  private String reportDataCatalogLink;

  private Boolean hasDataCatalogDescription;

  private String reviewStatus;

  private String reportConfidentiality;

  private String reportTrainingMaterials;

  private Long uniqueUsersLastMonth;

  private java.sql.Timestamp reportLastModifiedDate;

  private UserLdap hasTechnicalOwner;

  private RequestedReview requestedReview;

  public ReportResponse (
    UUID reportId,
    String reportName,
    UUID reportDataCatalogId,
    String reportType,
    String reportDescription,
    String reportLink,
    String reportTemplate,
    String reportDataCatalogLink,
    Boolean hasDataCatalogDescription,
    String reviewStatus,
    String reportConfidentiality,
    String reportTrainingMaterials,
    Long uniqueUsersLastMonth,
    java.sql.Timestamp reportLastModifiedDate,
    String technicalOwner,
    String reportReviewRequestId
  ) {
    this.reportId = reportId;
    this.reportName = reportName;
    this.reportDataCatalogId = reportDataCatalogId;
    this.reportType = reportType;
    this.reportDescription = reportDescription;
    this.reportLink = reportLink;
    this.reportTemplate = reportTemplate;
    this.reportDataCatalogLink = reportDataCatalogLink;
    this.hasDataCatalogDescription = hasDataCatalogDescription;
    this.reviewStatus = reviewStatus;
    this.reportConfidentiality = reportConfidentiality;
    this.reportTrainingMaterials = reportTrainingMaterials;
    this.uniqueUsersLastMonth = uniqueUsersLastMonth;
    this.reportLastModifiedDate = reportLastModifiedDate;

    if (StringUtils.isNotEmpty(technicalOwner)) {
      hasTechnicalOwner = new UserLdap(technicalOwner);
    }

    if (StringUtils.isNotEmpty(reportReviewRequestId)) {
      requestedReview = new RequestedReview(reportReviewRequestId);
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class RequestedReview {
    private String reportReviewRequestId;
  }
}
