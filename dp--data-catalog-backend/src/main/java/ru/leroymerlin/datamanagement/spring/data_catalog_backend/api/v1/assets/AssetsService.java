package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.ChildrenSortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.GetAssetHeaderResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostAssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

public interface AssetsService {
  PostAssetResponse createAsset (
    PostOrPatchAssetRequest assetRequest,
    User user
  ) throws
    StatusNotFoundException,
    IllegalArgumentException,
    AssetTypeNotFoundException,
    AssetTypeStatusAssignmentNotFoundException;

  AssetResponse updateAsset (
    UUID assetId,
    PostOrPatchAssetRequest assetRequest,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    AssetTypeNotFoundException,
    AssetTypeStatusAssignmentNotFoundException;

  GetAssetsResponse getAssetsByParams (
    GetAssetParams getAssetParams
  );

  AssetResponse getAssetById (UUID assetId) throws AssetNotFoundException;

  void deleteAssetById (UUID assetId, User user) throws AssetNotFoundException;

  GetAssetsChildrenResponse getAssetsChildren (
    UUID assetId,
    String assetDisplayname,
    List<UUID> assetTypeIds,
    List<UUID> lifecycleStatusIds,
    List<UUID> stewardshipStatusIds,
    ChildrenSortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  );

  GetAssetPathElementsResponse getAssetPath (UUID assetId);

  GetAssetRelationTypes getAssetRelationTypes (UUID assetId);

  GetAssetAttributeLinksUsageResponse getAssetAttributeLinkUsage (
    UUID assetId,
    List<UUID> assetTypeIds,
    List<UUID> attributeTypeIds,
    List<UUID> lifecycleStatusIds,
    List<UUID> stewardshipStatusIds,
    Integer pageNumber,
    Integer pageSize
  );

  List<PostAssetResponse> createAssetsBulk (
    List<PostOrPatchAssetRequest> assetsRequest,
    User user
  ) throws
    StatusNotFoundException,
    IllegalArgumentException,
    AssetTypeNotFoundException,
    InvalidFieldLengthException,
    AssetTypeStatusAssignmentNotFoundException;

  void deleteAssetsBulk (List<UUID> assetsRequest, User user) throws AssetNotFoundException;

  GetAssetHeaderResponse getAssetHeader (UUID assetId);

  List<AssetResponse> updateBulkAsset (List<PatchAssetRequest> assetsRequest, User user);

  GetAssetChangeHistory getAssetChangeHistory (UUID assetId, GetChangeHistoryParams params);
}
