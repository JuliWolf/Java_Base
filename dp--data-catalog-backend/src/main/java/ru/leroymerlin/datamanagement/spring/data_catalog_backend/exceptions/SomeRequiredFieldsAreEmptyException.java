package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions;

import java.util.Map;
import lombok.Getter;

@Getter
public class SomeRequiredFieldsAreEmptyException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public SomeRequiredFieldsAreEmptyException () {
    super("Some of required fields are empty.");
  }

  public SomeRequiredFieldsAreEmptyException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
