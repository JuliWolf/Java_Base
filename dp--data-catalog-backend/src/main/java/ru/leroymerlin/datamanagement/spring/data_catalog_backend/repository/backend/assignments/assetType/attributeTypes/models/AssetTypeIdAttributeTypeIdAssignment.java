package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetTypeIdAttributeTypeIdAssignment {
  private UUID attributeTypeId;

  private UUID assetTypeId;
}
