package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions;

/**
 * @author juliwolf
 */

public class RequestError extends RuntimeException {
  public RequestError () {
    super("Request error");
  }
}
