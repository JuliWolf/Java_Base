package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models;

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
public class AssetTypeAttributeTypeAssignmentUsageCount {
  private UUID assetTypeAttributeTypeAssignmentId;

  private Long count;
}
