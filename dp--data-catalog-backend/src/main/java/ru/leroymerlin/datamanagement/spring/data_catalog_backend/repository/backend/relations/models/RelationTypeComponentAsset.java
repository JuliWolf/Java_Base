package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models;

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
public class RelationTypeComponentAsset {
  private UUID relationTypeComponentId;

  private UUID assetId;
}
