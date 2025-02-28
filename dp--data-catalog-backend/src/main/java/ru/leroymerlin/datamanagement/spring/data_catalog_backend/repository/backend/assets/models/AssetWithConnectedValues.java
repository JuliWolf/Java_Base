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
public class AssetWithConnectedValues {
  private UUID assetId;

  private String assetName;

  private String assetDisplayName;

  private UUID assetTypeId;

  private String assetTypeName;

  private UUID lifecycleStatusId;

  private String lifecycleStatusName;

  private UUID stewardshipStatusId;

  private String stewardshipStatusName;

  private String languageName;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  private java.sql.Timestamp lastModifiedOn;

  private UUID lastModifiedBy;

  private Boolean isDeleted;
}
