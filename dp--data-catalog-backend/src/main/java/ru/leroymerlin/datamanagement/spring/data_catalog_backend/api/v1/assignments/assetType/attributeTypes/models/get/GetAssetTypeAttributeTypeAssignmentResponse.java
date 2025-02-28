package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetAssetTypeAttributeTypeAssignmentResponse {
  private UUID asset_attribute_id;

  private UUID attribute_type_id;

  private String attribute_type_name;

  private String attribute_type_description;

  private AttributeKindType attribute_kind;

  private String validation_mask;

  private List<String> allowed_values;

  private Boolean inherited_flag;

  private UUID parent_asset_type_id;

  private String parent_asset_type_name;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
