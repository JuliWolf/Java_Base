package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions;

import java.util.Map;
import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public class DuplicateValueInRequestException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public DuplicateValueInRequestException (String error) {
    super(error);
  }

  public DuplicateValueInRequestException (String error, Map<String, Object> details) {
    super(error);

    this.details = details;
  }
}
