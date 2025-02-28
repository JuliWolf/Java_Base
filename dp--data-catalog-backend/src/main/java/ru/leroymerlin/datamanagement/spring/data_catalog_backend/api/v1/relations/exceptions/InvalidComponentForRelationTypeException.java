package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author juliwolf
 */

@Getter
public class InvalidComponentForRelationTypeException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public InvalidComponentForRelationTypeException () {
    super("Invalid component for this relation type.");
  }

  public InvalidComponentForRelationTypeException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
