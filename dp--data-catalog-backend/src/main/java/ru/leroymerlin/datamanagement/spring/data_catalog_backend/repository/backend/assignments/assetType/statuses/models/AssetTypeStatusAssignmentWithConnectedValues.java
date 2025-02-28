package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AssetTypeStatusAssignmentWithConnectedValues {
  private UUID assetTypeId;

  private String assetTypeName;

  private UUID assetTypeStatusAssignmentId;

  private UUID statusId;

  private String statusName;

  private String statusDescription;

  private Boolean isInherited;

  private UUID parentAssetTypeId;

  private String parentAssetTypeName;

  private AssignmentStatusType assignmentStatusType;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;
}
