package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions;

/**
 * @author JuliWolf
 */
public class AttributeWithAttributeTypeExistsException extends RuntimeException {
  public AttributeWithAttributeTypeExistsException () {
    super("Attribute with this attribute type still exists.");
  }

}
