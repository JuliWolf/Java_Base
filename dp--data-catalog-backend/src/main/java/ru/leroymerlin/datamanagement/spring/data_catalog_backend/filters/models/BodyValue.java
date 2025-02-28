package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.models;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author juliwolf
 */

public record BodyValue (
  String method,

  String pathPattern,

  HttpServletRequest request,

  BodyValueCallback callback
) {
}
