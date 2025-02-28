package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class RelationComponentNotFoundException extends RuntimeException {
  public RelationComponentNotFoundException () {
    super("relation component not found");
  }

  public RelationComponentNotFoundException (UUID relationComponentId) {
    super("Relation component with id "+ relationComponentId + " not found");
  }
}
