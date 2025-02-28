package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions;

import java.util.Map;
import lombok.Getter;

@Getter
public class InvalidFieldLengthException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public InvalidFieldLengthException(String fieldName, int maxLength) {
    super(fieldName + " contains too much symbols. Allowed limit is " + maxLength);
  }

  public InvalidFieldLengthException(Map<String, Object> details, String fieldName, int maxLength) {
    this(fieldName, maxLength);

    this.details = details;
  }
}
