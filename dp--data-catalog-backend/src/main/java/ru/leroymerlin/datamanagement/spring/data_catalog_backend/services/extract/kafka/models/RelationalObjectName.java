package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models;

import lombok.Getter;

/**
 * @author juliwolf
 */

public enum RelationalObjectName {
  CANON_VIEW("canon_view_topic"),

  CANON_COLUMN("canon_column_topic"),

  CANON_TABLE("canon_table_topic"),

  CANON_SCHEMA("canon_schema_topic");

  @Getter
  private String value;

  RelationalObjectName (String value) {
    this.value = value;
  }

  public static RelationalObjectName fromString(String text) {
    for (RelationalObjectName relationalObjectName : RelationalObjectName.values()) {
      if (relationalObjectName.value.equalsIgnoreCase(text)) {
        return relationalObjectName;
      }
    }
    return null;
  }
}
