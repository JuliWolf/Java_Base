package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class RelationAttributeNotFoundException extends RuntimeException {
  public RelationAttributeNotFoundException () {
    super("relation attribute not found");
  }

  public RelationAttributeNotFoundException (UUID relationAttribute) {
    super("Relation attribute with id " + relationAttribute + " not found");
  }
}
