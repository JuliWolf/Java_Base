package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum QueryType {
  TABLE("Table"),

  HEADER("Header");

  private String value;

  QueryType (String value) {
    this.value = value;
  }
}
