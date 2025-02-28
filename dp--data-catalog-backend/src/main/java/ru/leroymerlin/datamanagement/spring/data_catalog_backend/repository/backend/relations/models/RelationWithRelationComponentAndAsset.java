package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models;

import java.util.UUID;
import lombok.*;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(exclude = {"relationComponentId", "relationTypeComponentId", "assetId"})
public class RelationWithRelationComponentAndAsset {
  private UUID relationId;

  private UUID relationTypeId;

  private UUID relationComponentId;

  private UUID relationTypeComponentId;

  private UUID assetId;
}
