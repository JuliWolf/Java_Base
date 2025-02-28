package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.models;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface AssetTypeWithRootFlag {

  String getAssetTypeIdText();
  default UUID getAssetTypeId () {
    if (StringUtils.isEmpty(getAssetTypeIdText())) return null;

    return UUID.fromString(getAssetTypeIdText());
  };

  String getAssetTypeName();

  String getAssetTypeDescription();

  String getLanguageName();

  String getAssetTypeAcronym();

  String getAssetTypeColor();

  String getAssetNameValidationMask();

  String getAssetNameValidationMaskExample();

  Long getInheritanceCount();

  java.sql.Timestamp getCreatedOn();

  String getCreatedByText();

  default UUID getCreatedBy () {
    if (StringUtils.isEmpty(getCreatedByText())) return null;

    return UUID.fromString(getCreatedByText());
  };

  java.sql.Timestamp getLastModifiedOn();

  String getLastModifiedByText();

  default UUID getLastModifiedBy () {
    if (StringUtils.isEmpty(getLastModifiedByText())) return null;

    return UUID.fromString(getLastModifiedByText());
  };

  default Boolean getRootFlag () {
    return getInheritanceCount() == 0;
  }
}
