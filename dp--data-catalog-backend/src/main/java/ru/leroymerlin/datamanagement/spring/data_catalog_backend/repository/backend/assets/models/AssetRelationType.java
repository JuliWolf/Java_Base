package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface AssetRelationType {
  String getRelationTypeIdText();

  default UUID getRelationTypeId () {
    if (StringUtils.isNoneEmpty(getRelationTypeIdText())) {
      return UUID.fromString(getRelationTypeIdText());
    }

    return null;
  }

  String getRelationTypeName();

  Long getTotal();
}
