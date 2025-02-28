package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models;

import java.util.UUID;

/**
 * @author juliwolf
 */

public interface AssetHierarchyPathElement {
  String getAssetName();

  String getAssetDisplayName();

  String getAssetIdText();
  default UUID getAssetId () {
    return UUID.fromString(getAssetIdText());
  }
}
