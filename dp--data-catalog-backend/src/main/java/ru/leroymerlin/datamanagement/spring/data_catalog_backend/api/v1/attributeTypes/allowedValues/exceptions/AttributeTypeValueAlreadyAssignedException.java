package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions;

/**
 * @author JuliWolf
 */
public class AttributeTypeValueAlreadyAssignedException extends RuntimeException {
  public AttributeTypeValueAlreadyAssignedException () {
    super("This value is already assigned to this attribute_type.");
  }
}
