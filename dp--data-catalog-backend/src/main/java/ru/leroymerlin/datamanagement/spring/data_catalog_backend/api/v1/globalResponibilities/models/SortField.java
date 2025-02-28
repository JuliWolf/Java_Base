package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum SortField {
  RESPONSIBLE_NAME("u.firstName"),

  ROLE_NAME("r.roleName");

  private String value;

  SortField (String value) {
    this.value = value;
  }
}
