package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions;

/**
 * @author juliwolf
 */

public class AttributeTypeNotAllowedForRelationException extends RuntimeException {
  public AttributeTypeNotAllowedForRelationException () {
    super("This attribute type is not allowed to be used for this relation.");
  }
}
