package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.models;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;

@FunctionalInterface
public interface PathVariableCallback {
  void loadData (UUID uuid, String key, RequestValues requestValues, AuthUserDetails userDetails);
}
