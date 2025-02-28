package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class BusinessFunctionNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public BusinessFunctionNotFoundException () {
    super("business function not found");
  }

  public BusinessFunctionNotFoundException (UUID businessFunctionId) {
    super("Business function with id " + businessFunctionId + " not found");
  }

  public BusinessFunctionNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
