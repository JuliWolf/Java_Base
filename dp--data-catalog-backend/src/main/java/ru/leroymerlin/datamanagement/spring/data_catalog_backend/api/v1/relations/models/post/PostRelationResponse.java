package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = {"relation_component_id", "hierarchy_role", "responsibility_inheritance_role", "created_on", "created_by"})
public class PostRelationResponse {
  private UUID relation_component_id;

  private UUID asset_id;

  private UUID relation_type_component_id;

  private HierarchyRole hierarchy_role;

  private ResponsibilityInheritanceRole responsibility_inheritance_role;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
