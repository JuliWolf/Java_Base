package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions;

/**
 * @author juliwolf
 */

public class BulkRequestArrayTooLargeException extends RuntimeException {
  public BulkRequestArrayTooLargeException () {
    super("Request array too large");
  }
}
