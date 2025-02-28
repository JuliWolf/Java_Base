package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get;

import java.util.List;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class GetGlobalResponsibilitiesResponse implements Response {
  long total;

  int page_size;

  int page_number;

  private List<GetGlobalResponsibilityResponse> results;
}
