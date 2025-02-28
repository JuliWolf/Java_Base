package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@Getter
@Setter
public class RelationAssetId {
  private UUID relationTypeId;

  private UUID assetId;
}
