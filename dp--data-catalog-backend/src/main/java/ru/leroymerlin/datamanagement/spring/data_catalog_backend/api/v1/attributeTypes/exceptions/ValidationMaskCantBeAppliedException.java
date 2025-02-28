package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions;

/**
 * @author JuliWolf
 */
public class ValidationMaskCantBeAppliedException extends RuntimeException {
  public ValidationMaskCantBeAppliedException () {
    super("Validation mask can't be applied to this attribute_type");
  }

}
