package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StageRelationAssetNk {
  private Long stageRelationId;

  private String asset1Nk;

  private String asset2Nk;
}
