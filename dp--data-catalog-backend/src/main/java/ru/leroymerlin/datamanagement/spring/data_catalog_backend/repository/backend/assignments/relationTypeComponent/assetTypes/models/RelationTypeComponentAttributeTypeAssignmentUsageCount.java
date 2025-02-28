package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models;

import java.util.UUID;

/**
 * @author juliwolf
 */

public interface RelationTypeComponentAttributeTypeAssignmentUsageCount {
  String getRelationTypeComponentAttributeTypeAssignmentIdText();

  default UUID getRelationTypeComponentAttributeTypeAssignmentId() {
    if (getRelationTypeComponentAttributeTypeAssignmentIdText() == null) return null;

    return UUID.fromString(getRelationTypeComponentAttributeTypeAssignmentIdText());
  }

  String getRelationTypeComponentIdText();

  default UUID getRelationTypeComponentId() {
    if (getRelationTypeComponentIdText() == null) return null;

    return UUID.fromString(getRelationTypeComponentIdText());
  }

  String getRelationTypeComponentName();

  String getAttributeTypeIdText();

  default UUID getAttributeTypeId() {
    if (getAttributeTypeIdText() == null) return null;

    return UUID.fromString(getAttributeTypeIdText());
  }

  String getAttributeTypeName();

  Long getCount();

  java.sql.Timestamp getCreatedOn();

  String getCreatedByText();

  default UUID getCreatedBy() {
    if (getCreatedByText() == null) return null;

    return UUID.fromString(getCreatedByText());
  }
}
