package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models;

/**
 * @author juliwolf
 */

public interface Operation {
  void execute(String query);
}
