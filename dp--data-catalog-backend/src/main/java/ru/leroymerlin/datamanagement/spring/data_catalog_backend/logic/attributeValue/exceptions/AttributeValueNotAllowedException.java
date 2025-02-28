package ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.ErrorWithDetail;

@Getter
public class AttributeValueNotAllowedException extends RuntimeExceptionWithDetails implements ErrorWithDetail {
  private Map<String, Object> details;

  public AttributeValueNotAllowedException () {
    super("Attribute value not allowed");
  }

  public AttributeValueNotAllowedException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
