package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssetAttributeLinkUsage {
  private UUID assetId;

  private String assetName;

  private String assetDisplayName;

  private UUID assetTypeId;

  private String assetTypeName;

  private UUID stewardshipStatusId;

  private String stewardshipStatusName;

  private UUID lifecycleStatusId;

  private String lifecycleStatusName;

  private UUID attributeTypeId;

  private String attributeTypeName;

  private String value;
}
