package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationAssetId;

/**
 * @author juliwolf
 */

@Getter
@Setter
public class RelationConnectedValues {
  private List<UUID> assetIds;

  private UUID relationTypeId;

  public RelationConnectedValues (List<RelationAssetId> relationAssetIds) {
    this.relationTypeId = relationAssetIds.get(0).getRelationTypeId();
    this.assetIds = relationAssetIds.stream().map(RelationAssetId::getAssetId).toList();
  }
}
