package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetRelationTypesResponse implements Response {
  long total;

  int page_size;

  int page_number;

  private List<GetRelationTypeResponse> results;
}
