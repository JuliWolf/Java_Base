package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author juliwolf
 */

@Getter
public class RelationTypeDoesNotAllowedRelatedAssetException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public RelationTypeDoesNotAllowedRelatedAssetException () {
    super("This relation type doesn't allow self related assets");
  }

  public RelationTypeDoesNotAllowedRelatedAssetException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
