package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.models;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface StatusUsageCount {
  String getStatusIdText();

  default UUID getStatusId() {
    if (StringUtils.isEmpty(getStatusIdText())) return null;

    return UUID.fromString(getStatusIdText());
  }

  Long getStatusUsageCount();
}
