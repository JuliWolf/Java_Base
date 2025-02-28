package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetMetadataExtractsResponse implements Response {
  private long total;

  private int page_size;

  private int page_number;

  List<GetMetadataExtractResponse> results;
}
