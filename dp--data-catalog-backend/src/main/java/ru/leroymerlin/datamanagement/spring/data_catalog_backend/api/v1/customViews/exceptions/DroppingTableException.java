package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions;

/**
 * @author juliwolf
 */

public class DroppingTableException extends RuntimeException {
  public DroppingTableException () {
    super("Dropping backend tables is strictly FORBIDDEN.");
  }
}
