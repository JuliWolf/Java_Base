package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models;

import java.util.UUID;
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
public class DeleteAsset {
  private Long stageAssetId;

  private UUID matchedAssetId;
  public UUID getRequest () {
    return this.matchedAssetId;
  }
}
