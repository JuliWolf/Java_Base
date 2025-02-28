package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRelationTypeComponentRequest extends RelationTypeComponentWithRoles {
  private String relation_type_component_name;

  private String relation_type_component_description;

  private String responsibility_inheritance_role;

  private String hierarchy_role;

  private Boolean single_relation_type_component_for_asset_flag;
}
