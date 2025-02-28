package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRelationTypeComponentResponse {
  private UUID relation_type_component_id;

  private String relation_type_component_name;

  private String relation_type_component_description;

  private ResponsibilityInheritanceRole responsibility_inheritance_role;

  private HierarchyRole hierarchy_role;

  private Boolean single_relation_type_component_for_asset_flag;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
