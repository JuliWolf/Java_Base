package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions;

import java.util.UUID;

/**
 * @author juliwolf
 */

public class RelationTypeAttributeTypeAssignmentNotFound extends RuntimeException {
  public RelationTypeAttributeTypeAssignmentNotFound () {
    super("relation type attribute type assignment not found");
  }

  public RelationTypeAttributeTypeAssignmentNotFound (UUID relationTypeAttributeTypeAssignmentId) {
    super("Relation type attribute type assignment with id "+ relationTypeAttributeTypeAssignmentId + " not found");
  }
}
