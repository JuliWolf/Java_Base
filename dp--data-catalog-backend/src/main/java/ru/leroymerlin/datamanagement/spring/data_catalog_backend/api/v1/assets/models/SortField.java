package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum SortField {
  ASSET_NAME("assetName"),

  ASSET_DISPLAYNAME("assetDisplayName");

  private String value;

  SortField (String value) {
    this.value = value;
  }
}
