package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions;

/**
 * @author juliwolf
 */

public class CustomViewTableQueryIsEmptyException extends RuntimeException {
  public CustomViewTableQueryIsEmptyException () {
    super("Custom view table query is empty");
  }
}
