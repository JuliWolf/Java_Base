package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions;

import java.util.Map;
import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public class RuntimeExceptionWithDetails extends RuntimeException {
  private Map<String, Object> details;

  public RuntimeExceptionWithDetails (String message) {
    super(message);
  }

  public RuntimeExceptionWithDetails (String message, Map<String, Object> details) {
    this(message);

    this.details = details;
  }
}
