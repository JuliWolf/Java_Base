package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class RelationTypeNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public RelationTypeNotFoundException () {
    super("relation type not found");
  }

  public RelationTypeNotFoundException (UUID relationTypeId) {
    super("Relation type with id "+ relationTypeId + " not found");
  }

  public RelationTypeNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
