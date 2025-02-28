package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class RelationNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public RelationNotFoundException () {
    super("relation not found");
  }

  public RelationNotFoundException (UUID relationId) {
    super("Relation with id "+ relationId + " not found");
  }

  public RelationNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
