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
public class RelationComponentWithConnectedValues {
  private UUID relationId;

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
}
