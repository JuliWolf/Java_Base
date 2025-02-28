package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models;

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
public class RelationTypeComponentAssetTypeAssignmentWithConnectedValues {
  private UUID assetTypeId;

  private String assetTypeName;

  private UUID relationTypeComponentAssetTypeAssignmentId;

  private Boolean isInherited;

  private UUID parentAssetTypeId;

  private String parentAssetTypeName;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;
}
