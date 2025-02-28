package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetRelationComponentWithAttributesResponse implements Response {
  private UUID relation_component_id;

  private UUID asset_id;

  private String asset_displayname;

  private List<GetRelationComponentAttributesResponse> relation_component_attributes;

  private HierarchyRole hierarchy_role;

  private ResponsibilityInheritanceRole responsibility_inheritance_role;

  private UUID relation_type_component_id;

  private String relation_type_component_name;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
