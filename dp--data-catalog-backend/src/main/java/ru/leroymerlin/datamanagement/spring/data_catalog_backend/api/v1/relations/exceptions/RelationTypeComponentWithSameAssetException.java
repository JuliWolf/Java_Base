package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author juliwolf
 */

@Getter
public class RelationTypeComponentWithSameAssetException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public RelationTypeComponentWithSameAssetException () {
    super("It's not allowed to use this relation type component with the same asset more than once.");
  }

  public RelationTypeComponentWithSameAssetException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
