package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.SortField;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAssetParams {
  private String assetName;

  private String assetDisplayName;

  private AssetSearchMode assetSearchMode;

  private Boolean isSearchAny;

  private List<UUID> assetTypeIds;

  private List<UUID> lifecycleStatuses;

  private List<UUID> stewardshipStatuses;

  private Boolean rootFlag;

  private SortField sortField;

  private SortOrder sortOrder;

  private Integer pageNumber;

  private Integer pageSize;
}
