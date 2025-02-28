package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetAttributeLinkUsage;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAssetAttributeLinkResponse implements Response {
  private UUID asset_id;

  private String asset_name;

  private String asset_displayname;

  private UUID asset_type_id;

  private String asset_type_name;

  private UUID stewardship_status_id;

  private String stewardship_status_name;

  private UUID lifecycle_status_id;

  private String lifecycle_status_name;

  private UUID attribute_type_id;

  private String attribute_type_name;

  private String value;

  public GetAssetAttributeLinkResponse (AssetAttributeLinkUsage linkUsage) {
    this.asset_id = linkUsage.getAssetId();
    this.asset_name = linkUsage.getAssetName();
    this.asset_displayname = linkUsage.getAssetDisplayName();
    this.asset_type_id = linkUsage.getAssetTypeId();
    this.asset_type_name = linkUsage.getAssetTypeName();
    this.stewardship_status_id = linkUsage.getStewardshipStatusId();
    this.stewardship_status_name = linkUsage.getStewardshipStatusName();
    this.lifecycle_status_id = linkUsage.getLifecycleStatusId();
    this.lifecycle_status_name = linkUsage.getLifecycleStatusName();
    this.attribute_type_id = linkUsage.getAttributeTypeId();
    this.attribute_type_name = linkUsage.getAttributeTypeName();
    this.value = linkUsage.getValue();
  }
}
