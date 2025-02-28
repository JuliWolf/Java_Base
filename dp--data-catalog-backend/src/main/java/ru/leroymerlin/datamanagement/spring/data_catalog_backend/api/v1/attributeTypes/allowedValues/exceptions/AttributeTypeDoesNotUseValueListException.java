package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions;

/**
 * @author JuliWolf
 */
public class AttributeTypeDoesNotUseValueListException extends RuntimeException {
  public AttributeTypeDoesNotUseValueListException () {
    super("This attribute type doesn't use value list.");
  }
}
