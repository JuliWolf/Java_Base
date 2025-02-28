package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get;

import java.util.List;
import java.util.UUID;
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
public class GetBusinessFunctionBusinessTermsResponse implements Response {
  private long total;

  private int pageSize;

  private int pageNumber;

  private List<GetBusinessTermResponse> businessTerms;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class GetBusinessTermResponse implements Response {
    private UUID businessTermId;
  }
}
