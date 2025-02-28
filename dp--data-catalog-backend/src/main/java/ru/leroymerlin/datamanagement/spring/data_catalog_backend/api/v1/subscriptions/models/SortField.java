package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum SortField {
  USERNAME("u.username"),

  ASSET_NAME("a.assetName");

  private String value;

  SortField (String value) {
    this.value = value;
  }
}
