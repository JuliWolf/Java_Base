package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PostAssetTypeAttributeAssignmentResponse {
  private UUID asset_attribute_id;

  private UUID asset_type_id;

  private UUID attribute_type_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
