package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public interface AssetIdResponse {
  String getAssetIdText();

  default UUID getAssetId () {
    if (StringUtils.isEmpty(getAssetIdText())) return null;

    return UUID.fromString(getAssetIdText());
  }
}
