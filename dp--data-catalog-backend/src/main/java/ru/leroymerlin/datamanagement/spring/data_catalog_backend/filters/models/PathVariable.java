package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.models;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author juliwolf
 */

public record PathVariable(
  String pathVariable,
  String pathPattern,
  HttpServletRequest request,
  String method,
  String pathVariableKey,

  PathVariableCallback callback
) {
}

