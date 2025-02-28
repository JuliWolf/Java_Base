package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.ReportResponse;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetReportsResponse implements Response {
  private long total;

  private int pageSize;

  private int pageNumber;

  private List<ReportResponse> results;
}
