package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models;

import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class HierarchyRoleAssets {
  private Asset parentAsset;

  private Asset childAsset;
}
