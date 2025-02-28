package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeComponentResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetRelationTypeComponentWithAssignmentsResponse implements RelationTypeComponentResponse {
  private UUID relation_type_component_id;

  private String relation_type_component_name;

  private String relation_type_component_description;

  private ResponsibilityInheritanceRole responsibility_inheritance_role;

  private HierarchyRole hierarchy_role;

  private Boolean single_relation_type_component_for_asset_flag;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;

  private List<RelationTypeComponentAssetTypeAssignment> relation_type_component_asset_type_assignments;

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RelationTypeComponentAssetTypeAssignment {
    private UUID relation_type_component_asset_type_assignment_id;

    private UUID asset_type_id;
  }
}
