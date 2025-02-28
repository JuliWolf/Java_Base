package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.AssetTypeRelationTypeComponentAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAssetTypeRelationTypeComponentAssignment implements Response {
  private UUID relation_type_component_asset_type_assignment_id;

  private Boolean inherited_flag;

  private UUID parent_asset_type_id;

  private String parent_asset_type_name;

  private UUID relation_type_component_id;

  private String relation_type_component_name;

  private String relation_type_component_description;

  private ResponsibilityInheritanceRole responsibility_inheritance_role;

  private HierarchyRole hierarchy_role;

  private UUID relation_type_id;

  private String relation_type_name;

  private String relation_type_description;

  private Integer component_number;

  private Boolean responsibility_inheritance_flag;

  private Boolean hierarchy_flag;

  private Boolean uniqueness_flag;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  public GetAssetTypeRelationTypeComponentAssignment (AssetTypeRelationTypeComponentAssignment assignment) {
    this.relation_type_component_asset_type_assignment_id = assignment.getRelationTypeComponentAssetTypeAssignmentId();
    this.inherited_flag = assignment.getInheritedFlag();
    this.parent_asset_type_id = assignment.getParentAssetTypeId();
    this.parent_asset_type_name = assignment.getParentAssetTypeName();
    this.relation_type_component_id = assignment.getRelationTypeComponentId();
    this.relation_type_component_name = assignment.getRelationTypeComponentName();
    this.relation_type_component_description = assignment.getRelationTypeComponentDescription();
    this.responsibility_inheritance_role = assignment.getResponsibilityInheritanceRole();
    this.hierarchy_role = assignment.getHierarchyRole();
    this.relation_type_id = assignment.getRelationTypeId();
    this.relation_type_name = assignment.getRelationTypeName();
    this.relation_type_description = assignment.getRelationTypeDescription();
    this.component_number = assignment.getComponentNumber();
    this.responsibility_inheritance_flag = assignment.getRelationTypeResponsibilityInheritanceFlag();
    this.hierarchy_flag = assignment.getRelationTypeHierarchyFlag();
    this.uniqueness_flag = assignment.getUniquenessFlag();
    this.created_on = assignment.getCreatedOn();
    this.created_by = assignment.getCreatedBy();
  }
}
