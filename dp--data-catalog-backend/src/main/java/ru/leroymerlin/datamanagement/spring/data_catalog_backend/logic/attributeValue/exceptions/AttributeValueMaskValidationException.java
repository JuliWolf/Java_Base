package ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.ErrorWithDetail;

@Getter
public class AttributeValueMaskValidationException extends RuntimeExceptionWithDetails implements ErrorWithDetail {
  private Map<String, Object> details;

  public AttributeValueMaskValidationException () {
    super("Attribute value doesn't match the mask");
  }

  public AttributeValueMaskValidationException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
