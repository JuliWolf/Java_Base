package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get;

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
public class GetAssetTypeChildrenResponse implements Response {
  long total;

  int page_size;

  int page_number;

  List<GetAssetTypeChildResponse> results;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class GetAssetTypeChildResponse implements Response {
    private UUID asset_type_id;

    private String asset_type_name;

    private String asset_type_description;

    private Integer children_asset_type_count;
  }
}
