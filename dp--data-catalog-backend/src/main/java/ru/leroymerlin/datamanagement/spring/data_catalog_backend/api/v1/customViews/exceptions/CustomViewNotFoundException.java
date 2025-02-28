package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class CustomViewNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public CustomViewNotFoundException () {
    super("custom view not found");
  }

  public CustomViewNotFoundException (UUID customViewId) {
    super("Custom view with id " + customViewId + " not found");
  }

  public CustomViewNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
