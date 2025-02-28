package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class BusinessTermNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public BusinessTermNotFoundException () {
    super("business term not found");
  }

  public BusinessTermNotFoundException (UUID businessTermId) {
    super("Business term with id " + businessTermId + " not found");
  }

  public BusinessTermNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
