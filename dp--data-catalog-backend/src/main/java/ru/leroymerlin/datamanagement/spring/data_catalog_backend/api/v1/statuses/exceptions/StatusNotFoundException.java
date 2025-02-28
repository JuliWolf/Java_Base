package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class StatusNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public StatusNotFoundException (UUID statusId) {
    super("Status with id " + statusId + " not found");
  }

  public StatusNotFoundException () {
    super("status not found");
  }

  public StatusNotFoundException(Map<String, Object> details) {
    this();

    this.details = details;
  }
}
