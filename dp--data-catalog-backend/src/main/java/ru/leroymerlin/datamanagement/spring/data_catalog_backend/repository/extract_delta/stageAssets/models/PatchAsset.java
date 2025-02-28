package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatchAsset {
  private Long stageAssetId;

  private UUID matchedAssetId;

  private String assetName;

  private String assetDisplayname;

  private UUID assetTypeId;

  private UUID lifecycleStatus;

  private UUID stewardshipStatus;

  public PatchAssetRequest getRequest () {
    return new PatchAssetRequest(
      this.matchedAssetId,
      this.assetName,
      this.assetDisplayname,
      UUIDUtils.convertUUIDToString(this.assetTypeId),
      this.lifecycleStatus,
      this.stewardshipStatus
    );
  }
}
