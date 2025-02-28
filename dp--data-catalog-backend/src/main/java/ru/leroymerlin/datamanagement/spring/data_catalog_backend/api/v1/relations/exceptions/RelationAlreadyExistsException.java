package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author juliwolf
 */

@Getter
public class RelationAlreadyExistsException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public RelationAlreadyExistsException () {
    super("Relation with these parameters already exists.");
  }

  public RelationAlreadyExistsException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
