package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get;

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
public class GetCustomViewsResponse implements Response {
  long total;

  int page_size;

  int page_number;

  List<GetCustomViewResponse> results;
}
