package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssetTypeRelationTypeComponentAssignment {
  private UUID relationTypeComponentAssetTypeAssignmentId;

  private Boolean inheritedFlag;

  private UUID parentAssetTypeId;

  private String parentAssetTypeName;

  private UUID relationTypeComponentId;

  private String relationTypeComponentName;

  private String relationTypeComponentDescription;

  private ResponsibilityInheritanceRole responsibilityInheritanceRole;

  private HierarchyRole hierarchyRole;

  private UUID relationTypeId;

  private String relationTypeName;

  private String relationTypeDescription;

  private Integer componentNumber;

  private Boolean relationTypeResponsibilityInheritanceFlag;

  private Boolean relationTypeHierarchyFlag;

  private Boolean uniquenessFlag;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;
}
