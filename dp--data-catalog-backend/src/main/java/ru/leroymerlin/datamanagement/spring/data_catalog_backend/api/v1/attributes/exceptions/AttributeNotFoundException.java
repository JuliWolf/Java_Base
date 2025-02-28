package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class AttributeNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public AttributeNotFoundException () {
    super("attribute not found");
  }

  public AttributeNotFoundException (UUID attributeId) {
    super("Attribute with id " + attributeId + " not found");
  }

  public AttributeNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
