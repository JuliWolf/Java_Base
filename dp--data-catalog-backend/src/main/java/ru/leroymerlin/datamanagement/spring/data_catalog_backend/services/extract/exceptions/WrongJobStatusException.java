package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.exceptions;

/**
 * @author juliwolf
 */

public class WrongJobStatusException extends RuntimeException {
  public WrongJobStatusException (String message) {
    super(message);
  }
}
