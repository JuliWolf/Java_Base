package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author juliwolf
 */

@Getter
public class ResponsibilityIsInheritedException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public ResponsibilityIsInheritedException () {
    super("Deleting of inherited responsibilities is not allowed");
  }

  public ResponsibilityIsInheritedException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
