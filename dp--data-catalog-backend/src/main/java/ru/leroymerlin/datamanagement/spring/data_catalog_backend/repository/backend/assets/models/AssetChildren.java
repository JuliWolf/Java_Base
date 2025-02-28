package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface AssetChildren {
  String getAssetIdText();
  default UUID getAssetId () {
    return UUID.fromString(getAssetIdText());
  }

  String getName();

  String getDisplayname();

  String getAssetTypeText();

  default UUID getAssetTypeId () {
    return UUID.fromString(getAssetTypeText());
  }

  Integer getChildrenCount();

  String getAssetTypeName();

  String getDescription();

  String getStewardshipStatusIdText();

  default UUID getStewardshipStatusId () {
    if (StringUtils.isEmpty(getStewardshipStatusIdText())) return null;

    return UUID.fromString(getStewardshipStatusIdText());
  }

  String getStewardshipStatusName();

  String getLifecycleStatusIdText();

  default UUID getLifecycleStatusId () {
    if (StringUtils.isEmpty(getLifecycleStatusIdText())) return null;

    return UUID.fromString(getLifecycleStatusIdText());
  }

  String getLifecycleStatusName();
}
