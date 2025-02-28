package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PostBulkRelationsResponse {
  private UUID relation_id;

  private UUID relation_type_id;

  private List<PostBulkRelationComponentResponse> component;

  @NoArgsConstructor
  @AllArgsConstructor
  @Setter
  @Getter
  public static class PostBulkRelationComponentResponse {
    private UUID relation_component_id;

    private UUID asset_id;

    private UUID relation_type_component_id;

    private HierarchyRole hierarchy_role;

    private ResponsibilityInheritanceRole responsibility_inheritance_role;

    private java.sql.Timestamp created_on;

    private UUID created_by;
  }
}
