package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions;

/**
 * @author JuliWolf
 */
public class AttributeDoesNotMatchTheMaskException extends RuntimeException {
  public AttributeDoesNotMatchTheMaskException () {
    super("Some of current attributes with this attribute type don't match the mask.");
  }

}
