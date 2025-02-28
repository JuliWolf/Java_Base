package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions;

/**
 * @author juliwolf
 */

public class ErrorWhileParsingJsonException extends RuntimeException {
  public ErrorWhileParsingJsonException () {
    super("Error while parsing json string to json");
  }
}
