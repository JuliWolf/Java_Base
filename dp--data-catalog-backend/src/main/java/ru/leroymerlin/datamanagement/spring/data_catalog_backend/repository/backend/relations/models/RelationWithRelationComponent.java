package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models;

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
@Setter
@Getter
public class RelationWithRelationComponent {
  private UUID relationId;

  private UUID relationTypeId;

  private String relationTypeName;

  private Boolean responsibilityInheritanceFlag;

  private Boolean hierarchyFlag;

  private Boolean uniquenessFlag;

  private java.sql.Timestamp relationCreatedOn;

  private UUID relationCreatedBy;

  private UUID relationComponentId;

  private UUID assetId;

  private String assetDisplayName;

  private String assetName;

  private UUID assetTypeId;

  private String assetTypeName;

  private UUID stewardshipStatusId;

  private String stewardshipStatusName;

  private UUID lifecycleStatusId;

  private String lifecycleStatusName;

  private HierarchyRole hierarchyRole;

  private ResponsibilityInheritanceRole responsibilityInheritanceRole;

  private UUID relationTypeComponentId;

  private String relationTypeComponentName;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  public RelationWithRelationType getRelationWithRelationType () {
    return new RelationWithRelationType(
      relationId,
      relationTypeId,
      relationTypeName,
      responsibilityInheritanceFlag,
      hierarchyFlag,
      uniquenessFlag,
      relationCreatedOn,
      relationCreatedBy
    );
  }

  public RelationComponentWithConnectedValues getRelationComponentWithConnectedValues() {
    return new RelationComponentWithConnectedValues(
      relationId,
      relationComponentId,
      assetId,
      assetDisplayName,
      assetName,
      assetTypeId,
      assetTypeName,
      stewardshipStatusId,
      stewardshipStatusName,
      lifecycleStatusId,
      lifecycleStatusName,
      hierarchyRole,
      responsibilityInheritanceRole,
      relationTypeComponentId,
      relationTypeComponentName,
      createdOn,
      createdBy
    );
  }
}
