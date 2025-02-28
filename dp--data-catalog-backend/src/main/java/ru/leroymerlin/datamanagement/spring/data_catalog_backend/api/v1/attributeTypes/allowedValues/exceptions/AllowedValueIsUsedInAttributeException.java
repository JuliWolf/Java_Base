package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class AllowedValueIsUsedInAttributeException extends RuntimeException {
  public AllowedValueIsUsedInAttributeException (UUID attributeId) {
    super("This value is still used in attribute '" + attributeId + "'");
  }

}
