package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.get;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.AttributeResponse;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetAttributesResponse implements Response {
  long total;

  int page_size;

  int page_number;

  private List<AttributeResponse> results;
}
