package ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.ErrorWithDetail;

/**
 * @author JuliWolf
 */
@Getter
public class AttributeInvalidDataTypeException extends RuntimeExceptionWithDetails implements ErrorWithDetail {
  private Map<String, Object> details;

  public AttributeInvalidDataTypeException () {
    super("Invalid data type for the attribute");
  }

  public AttributeInvalidDataTypeException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
