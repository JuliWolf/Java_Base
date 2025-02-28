package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum SortField {
  USERNAME("username"),

  FIRSTNAME("firstName"),

  LASTNAME("lastName"),

  FIRSTNAME_LASTNAME("firstName_lastName"),

  LASTNAME_FIRSTNAME("lastName_firstName"),

  EMAIL("email");

  private String value;

  SortField (String value) {
    this.value = value;
  }
}
