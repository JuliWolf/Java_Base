package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RelationTypeAttributeTypeAssignmentWithConnectedValues {
  private UUID relationTypeId;

  private String relationTypeName;

  private UUID relationTypeAttributeTypeAssignmentId;

  private UUID attributeTypeId;

  private String attributeTypeName;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;
}
