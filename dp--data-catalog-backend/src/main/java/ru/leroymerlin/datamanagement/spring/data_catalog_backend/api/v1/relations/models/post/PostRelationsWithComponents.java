package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post;

import java.util.AbstractMap;
import java.util.List;
import java.util.UUID;

/**
 * @author juliwolf
 */

public interface PostRelationsWithComponents {
  UUID getRelationTypeId();

  List<AbstractMap.SimpleEntry<UUID, UUID>> getComponentAssetEntrySet();
}
