package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions;

/**
 * @author JuliWolf
 */
public class IncompatibleAttributeKindException extends RuntimeException {
  public IncompatibleAttributeKindException () {
    super("Incompatible attribute kind");
  }

}
