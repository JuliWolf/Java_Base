package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get;

import java.util.List;
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
public class GetRelationsResponse implements Response {
  long total;

  int page_size;

  int page_number;

  private List<GetRelationResponse> results;
}
