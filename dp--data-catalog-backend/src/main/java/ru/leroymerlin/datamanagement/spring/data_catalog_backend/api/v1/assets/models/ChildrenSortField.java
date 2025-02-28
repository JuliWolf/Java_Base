package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum ChildrenSortField {
  ASSET_NAME("name"),

  DISPLAYNAME("displayname");

  private String value;

  ChildrenSortField (String value) {
    this.value = value;
  }
}
