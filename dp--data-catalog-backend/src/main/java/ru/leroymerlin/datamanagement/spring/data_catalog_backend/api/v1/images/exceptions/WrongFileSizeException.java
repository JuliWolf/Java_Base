package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.exceptions;

/**
 * @author juliwolf
 */

public class WrongFileSizeException extends RuntimeException {
  public WrongFileSizeException (String maxSize) {
    super("File is too large. Max file size is " + maxSize);
  }
}
