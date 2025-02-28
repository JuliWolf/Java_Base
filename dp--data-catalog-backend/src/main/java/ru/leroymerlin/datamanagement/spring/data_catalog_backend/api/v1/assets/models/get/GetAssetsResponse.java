package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetAssetsResponse implements Response {
  long total;

  int page_size;

  int page_number;

  private List<AssetResponse> results;
}
