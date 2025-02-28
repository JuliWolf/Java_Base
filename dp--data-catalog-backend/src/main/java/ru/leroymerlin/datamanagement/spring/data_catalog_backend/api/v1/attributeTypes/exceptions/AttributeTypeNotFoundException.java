package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class AttributeTypeNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public AttributeTypeNotFoundException () {
    super("attribute type not found");
  }

  public AttributeTypeNotFoundException (UUID attributeTypeId) {
    super("Attribute type with id " + attributeTypeId + " not found");
  }

  public AttributeTypeNotFoundException(Map<String, Object> details) {
    this();

    this.details = details;
  }
}
