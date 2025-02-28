package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.Report;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostReportReviewRequest extends Report implements Request {
  private String reportReviewRequestId;

  private ReviewReport reviewedReport;

  private UserLdap hasReviewer;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class ReviewReport {
    private UUID reportDataCatalogId;
  }
}
