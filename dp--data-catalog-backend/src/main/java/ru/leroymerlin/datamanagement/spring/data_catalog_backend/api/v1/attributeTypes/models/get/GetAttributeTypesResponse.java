package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GetAttributeTypesResponse implements Response {
  long total;

  int page_size;

  int page_number;

  List<GetAttributeTypeResponse> results;
}
