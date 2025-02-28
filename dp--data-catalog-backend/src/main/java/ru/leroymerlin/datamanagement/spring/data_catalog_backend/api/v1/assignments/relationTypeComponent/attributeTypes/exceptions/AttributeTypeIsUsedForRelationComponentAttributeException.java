package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions;

/**
 * @author juliwolf
 */

public class AttributeTypeIsUsedForRelationComponentAttributeException extends RuntimeException {
  public AttributeTypeIsUsedForRelationComponentAttributeException () {
    super("Attribute type is used for relation component attribute");
  }
}
