package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions;

/**
 * @author juliwolf
 */

public class AttributeTypeNotAllowedForRelationComponentException extends RuntimeException {
  public AttributeTypeNotAllowedForRelationComponentException () {
    super("This attribute type is not allowed to be used for this relation component.");
  }
}
