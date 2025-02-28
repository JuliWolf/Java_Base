package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface BusinessRelationship {
  String getBusinessTermIdText();

  default UUID getBusinessTermId () {
    if (StringUtils.isEmpty(getBusinessTermIdText())) return null;

    return UUID.fromString(getBusinessTermIdText());
  }

  String getBusinessTermRelationIdText();

  default UUID getBusinessTermRelationId () {
    if (StringUtils.isEmpty(getBusinessTermRelationIdText())) return null;

    return UUID.fromString(getBusinessTermRelationIdText());
  }

  String getRelatedBusinessTermIdText();

  default UUID getRelatedBusinessTermId () {
    if (StringUtils.isEmpty(getRelatedBusinessTermIdText())) return null;

    return UUID.fromString(getRelatedBusinessTermIdText());
  }

  String getBusinessTermRelationCardinality();

  String getBusinessTermRelationshipName();

  String getBusinessTermRelationshipTechnicalName();
}
