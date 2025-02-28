package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class AllowedValueIsUsedInRelationAttributeException extends RuntimeException {
  public AllowedValueIsUsedInRelationAttributeException (UUID relationAttributeId) {
    super("This value is still used in relation attribute '" + relationAttributeId + "'");
  }

}
