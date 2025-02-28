package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Relation;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResponsibilityInheritanceRoleAssets {
  private Asset sourceAsset;

  private Asset consumerAsset;

  private Relation relation;
}
