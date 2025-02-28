package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetAssetChildrenResponse {
  private UUID asset_id;

  private String asset_name;

  private String asset_displayname;

  private UUID asset_type_id;

  private String asset_type_name;

  private String description;

  private UUID lifecycle_status_id;

  private String lifecycle_status_name;

  private UUID stewardship_status_id;

  private String stewardship_status_name;

  private Integer children_asset_count;
}
