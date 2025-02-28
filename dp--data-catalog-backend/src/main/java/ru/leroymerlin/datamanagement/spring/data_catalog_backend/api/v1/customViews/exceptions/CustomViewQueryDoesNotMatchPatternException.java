package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions;

import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.QueryType;

/**
 * @author juliwolf
 */

public class CustomViewQueryDoesNotMatchPatternException extends RuntimeException {
  public CustomViewQueryDoesNotMatchPatternException (QueryType queryType, String pattern) {
    super(queryType.getValue() + " query doesn't match '" + pattern + "' pattern");
  }
}
