package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post;

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
public class PatchReportReviewRequest extends Report implements Request {
  private UserLdap hasReviewer;

  private String reportReviewRequestClosedDate;
}
