package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum SortField {
  RESPONSIBLE_NAME("u.firstName"),

  RESPONSIBLE_LAST_NAME("u.lastName"),

  RESPONSIBLE_FULLNAME("fullname"),

  ASSET_NAME("a.assetName"),

  ASSET_DISPLAY("a.assetDisplayName"),

  ROLE_NAME("r.roleName");

  private String value;

  SortField (String value) {
    this.value = value;
  }
}
