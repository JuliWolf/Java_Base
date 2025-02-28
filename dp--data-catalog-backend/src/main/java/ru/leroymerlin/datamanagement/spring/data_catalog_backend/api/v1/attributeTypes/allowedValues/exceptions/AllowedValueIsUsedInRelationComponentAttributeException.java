package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class AllowedValueIsUsedInRelationComponentAttributeException extends RuntimeException {
  public AllowedValueIsUsedInRelationComponentAttributeException (UUID relationComponentAttributeId) {
    super("This value is still used in relation component attribute '" + relationComponentAttributeId + "'");
  }

}
