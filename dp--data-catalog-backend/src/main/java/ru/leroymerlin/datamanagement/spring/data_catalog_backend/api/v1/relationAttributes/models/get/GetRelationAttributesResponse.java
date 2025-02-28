package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get;

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
public class GetRelationAttributesResponse implements Response {
  private long total;

  private int page_size;

  private int page_number;

  List<GetRelationAttributeResponse> results;
}
