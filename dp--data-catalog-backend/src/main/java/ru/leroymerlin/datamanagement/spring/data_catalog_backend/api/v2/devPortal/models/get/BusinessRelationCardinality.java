package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.models.get;

import lombok.Getter;

/**
 * @author juliwolf
 */

public enum BusinessRelationCardinality {
  ONE_TO_ONE("1->1"),

  ONE_TO_MANY("1->n"),
  MANY_TO_ONE("n->1"),

  MANY_TO_MANY("n->n");

  @Getter
  private String value;

  BusinessRelationCardinality (String value) {
    this.value = value;
  }

  public static BusinessRelationCardinality fromString(String text) {
    for (BusinessRelationCardinality businessRelationCardinality : BusinessRelationCardinality.values()) {
      if (businessRelationCardinality.value.equalsIgnoreCase(text)) {
        return businessRelationCardinality;
      }
    }
    return null;
  }
}
