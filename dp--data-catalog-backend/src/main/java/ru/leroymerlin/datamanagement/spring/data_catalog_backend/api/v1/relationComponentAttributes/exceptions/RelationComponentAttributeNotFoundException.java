package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class RelationComponentAttributeNotFoundException extends RuntimeException {
  public RelationComponentAttributeNotFoundException () {
    super("relation component attribute not found");
  }

  public RelationComponentAttributeNotFoundException (UUID relationComponentAttribute) {
    super("Relation component attribute with id " + relationComponentAttribute + " not found");
  }
}
