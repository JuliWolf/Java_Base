package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.models;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface AssetTypeChild {
  String getAssetTypeIdText();

  default UUID getAssetTypeId() {
    if (StringUtils.isEmpty(getAssetTypeIdText())) return null;

    return UUID.fromString(getAssetTypeIdText());
  }

  String getAssetTypeName();

  String getAssetTypeDescription();

  Integer getChildrenAssetTypeCount();
}
