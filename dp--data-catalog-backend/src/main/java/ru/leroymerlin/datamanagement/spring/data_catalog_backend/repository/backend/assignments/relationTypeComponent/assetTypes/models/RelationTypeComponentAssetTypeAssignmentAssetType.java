package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models;

import java.util.UUID;
import lombok.*;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class RelationTypeComponentAssetTypeAssignmentAssetType {
  private UUID relationTypeComponentId;

  private UUID relationTypeComponentAssetTypeAssignmentId;

  private UUID assetTypeId;
}
