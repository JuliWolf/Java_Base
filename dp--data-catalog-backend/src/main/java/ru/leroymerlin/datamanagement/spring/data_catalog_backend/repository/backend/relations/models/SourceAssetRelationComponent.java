package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface SourceAssetRelationComponent {

  String getSourceAssetIdText();
  default UUID getSourceAssetId() {
    if (StringUtils.isEmpty(getSourceAssetIdText())) return null;

    return UUID.fromString(getSourceAssetIdText());
  }

  String getConsumerAssetIdText();
  default UUID getConsumerAssetId() {
    if (StringUtils.isEmpty(getConsumerAssetIdText())) return null;

    return UUID.fromString(getConsumerAssetIdText());
  }

  String getRelationIdText();
  default UUID getRelationId() {
    if (StringUtils.isEmpty(getRelationIdText())) return null;

    return UUID.fromString(getRelationIdText());
  }

  Long getConsumersCount();
}
