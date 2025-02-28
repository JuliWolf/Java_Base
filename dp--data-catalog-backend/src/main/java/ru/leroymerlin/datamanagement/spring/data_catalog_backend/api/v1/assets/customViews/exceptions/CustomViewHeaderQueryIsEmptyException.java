package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions;

/**
 * @author juliwolf
 */

public class CustomViewHeaderQueryIsEmptyException extends RuntimeException {
  public CustomViewHeaderQueryIsEmptyException () {
    super("Custom view header query is empty");
  }
}
