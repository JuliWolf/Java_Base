package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author juliwolf
 */

@Getter
public class InvalidAssetTypeForComponentException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public InvalidAssetTypeForComponentException () {
    super("Invalid asset type for the component");
  }

  public InvalidAssetTypeForComponentException (UUID relationTypeComponentId) {
    super("Invalid asset type for the component '" + relationTypeComponentId + "'");
  }

  public InvalidAssetTypeForComponentException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
