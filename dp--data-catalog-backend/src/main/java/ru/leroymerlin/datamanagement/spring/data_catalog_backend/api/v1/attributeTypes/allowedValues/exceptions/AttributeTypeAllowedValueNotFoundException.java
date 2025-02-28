package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class AttributeTypeAllowedValueNotFoundException extends RuntimeException {
  public AttributeTypeAllowedValueNotFoundException () {
    super("attribute type allowed value type not found");
  }

  public AttributeTypeAllowedValueNotFoundException (UUID attributeTypeAllowedValueId) {
    super("Attribute type allowed value with id " + attributeTypeAllowedValueId + " not found");
  }
}
