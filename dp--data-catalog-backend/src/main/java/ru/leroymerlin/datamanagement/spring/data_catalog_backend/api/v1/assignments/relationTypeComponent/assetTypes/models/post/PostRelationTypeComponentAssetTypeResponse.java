package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostRelationTypeComponentAssetTypeResponse {
  private UUID relation_type_component_asset_type_id;

  private UUID relation_type_component_id;

  private UUID asset_type_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
