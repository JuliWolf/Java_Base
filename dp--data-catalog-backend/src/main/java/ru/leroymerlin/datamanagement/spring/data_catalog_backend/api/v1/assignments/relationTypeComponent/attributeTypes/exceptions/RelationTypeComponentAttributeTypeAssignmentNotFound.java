package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions;

import java.util.UUID;

/**
 * @author juliwolf
 */

public class RelationTypeComponentAttributeTypeAssignmentNotFound extends RuntimeException {
  public RelationTypeComponentAttributeTypeAssignmentNotFound () {
    super("relation type component attribute type assignment not found");
  }

  public RelationTypeComponentAttributeTypeAssignmentNotFound (UUID relationTypeComponentAttributeTypeAssignmentId) {
    super("Relation type component attribute type assignment with id "+ relationTypeComponentAttributeTypeAssignmentId + " not found");
  }
}
