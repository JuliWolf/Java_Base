package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.models;

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
public class ParentChildAsset {
  private UUID parentAssetId;

  private UUID childAssetId;
}
