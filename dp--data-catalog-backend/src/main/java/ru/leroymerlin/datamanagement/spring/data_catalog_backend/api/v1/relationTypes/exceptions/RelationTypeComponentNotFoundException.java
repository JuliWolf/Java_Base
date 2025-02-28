package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class RelationTypeComponentNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public RelationTypeComponentNotFoundException () {
    super("relation type component not found");
  }

  public RelationTypeComponentNotFoundException (UUID relationTypeComponentId) {
    super("Relation type component with id "+ relationTypeComponentId + " not found");
  }

  public RelationTypeComponentNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
