package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class ResponsibilityNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public ResponsibilityNotFoundException () {
    super("responsibility not found");
  }

  public ResponsibilityNotFoundException (UUID responsibilityId) {
    super("Responsibility with id "+ responsibilityId + " not found");
  }

  public ResponsibilityNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
