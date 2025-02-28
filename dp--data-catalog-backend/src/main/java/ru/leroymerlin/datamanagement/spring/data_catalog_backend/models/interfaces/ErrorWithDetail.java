package ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces;

import java.util.Map;

/**
 * @author juliwolf
 */

public interface ErrorWithDetail {
  Map<String, Object> getDetails();

  String getMessage();
}
