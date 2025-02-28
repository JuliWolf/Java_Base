package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.structUnits.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class StructUnitNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public StructUnitNotFoundException (UUID structUnitId) {
    super("Struct unit with id " + structUnitId + " not found");
  }

  public StructUnitNotFoundException () {
    super("struct unit not found");
  }

  public StructUnitNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
