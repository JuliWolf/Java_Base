package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.exceptions;

/**
 * @author juliwolf
 */

public class WrongFileTypeException extends RuntimeException {
  public WrongFileTypeException (String contentType) {
    super(contentType + " is not allowed");
  }
}
