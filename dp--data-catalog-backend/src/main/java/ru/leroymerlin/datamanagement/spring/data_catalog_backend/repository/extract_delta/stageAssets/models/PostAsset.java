package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;

import java.util.UUID;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostAsset {
  private UUID jobId;

  private Long stageAssetId;

  private String assetName;

  private String assetDisplayname;

  private UUID assetTypeId;

  private UUID lifecycleStatus;

  private UUID stewardshipStatus;

  public PostOrPatchAssetRequest getRequest () {
    return new PostOrPatchAssetRequest(
      this.assetName,
      this.assetDisplayname,
      UUIDUtils.convertUUIDToString(this.assetTypeId),
      UUIDUtils.convertUUIDToString(this.lifecycleStatus),
      UUIDUtils.convertUUIDToString(this.stewardshipStatus)
    );
  }
}
