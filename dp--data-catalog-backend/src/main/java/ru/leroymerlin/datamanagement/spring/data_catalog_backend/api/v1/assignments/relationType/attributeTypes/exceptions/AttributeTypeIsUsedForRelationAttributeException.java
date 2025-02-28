package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions;

/**
 * @author juliwolf
 */

public class AttributeTypeIsUsedForRelationAttributeException extends RuntimeException {
  public AttributeTypeIsUsedForRelationAttributeException () {
    super("Attribute type is used for relation attribute");
  }
}
