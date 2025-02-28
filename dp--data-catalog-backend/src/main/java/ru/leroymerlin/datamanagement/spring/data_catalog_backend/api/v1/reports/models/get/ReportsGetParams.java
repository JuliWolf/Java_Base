package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ReportsGetParams {
  private Integer pageSize;

  private Integer pageNumber;

  private String reportName;

  private ReviewStatus reviewStatus;

  private ReportConfidentiality reportConfidentiality;

  private Long uniqueUsersLastMonthMin;

  private Long uniqueUsersLastMonthMax;

  private java.sql.Timestamp reportLastModifiedDateMin;

  private java.sql.Timestamp reportLastModifiedDateMax;

  private Boolean hasDataCatalogDescription;

  public String getReviewStatus () {
    if (reviewStatus != null) {
      return reviewStatus.getValue();
    }

    return null;
  }

  public String getReportConfidentiality () {
    if (reportConfidentiality != null) {
      return reportConfidentiality.toString();
    }

    return null;
  }
}
