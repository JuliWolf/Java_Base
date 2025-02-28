package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.models;

import java.util.UUID;

/**
 * @author juliwolf
 */

public interface RelationTypeComponentAttributeTypeAssignmentUsageCount {
  String getRelationTypeAttributeTypeAssignmentIdText();

  default UUID getRelationTypeAttributeTypeAssignmentId() {
    if (getRelationTypeAttributeTypeAssignmentIdText() == null) return null;

    return UUID.fromString(getRelationTypeAttributeTypeAssignmentIdText());
  }

  String getRelationTypeIdText();

  default UUID getRelationTypeId() {
    if (getRelationTypeIdText() == null) return null;

    return UUID.fromString(getRelationTypeIdText());
  }

  String getRelationTypeName();

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
