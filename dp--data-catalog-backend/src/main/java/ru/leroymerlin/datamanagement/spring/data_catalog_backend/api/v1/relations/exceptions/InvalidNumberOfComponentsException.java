package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author juliwolf
 */

@Getter
public class InvalidNumberOfComponentsException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public InvalidNumberOfComponentsException () {
    super("Invalid number of components");
  }

  public InvalidNumberOfComponentsException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
