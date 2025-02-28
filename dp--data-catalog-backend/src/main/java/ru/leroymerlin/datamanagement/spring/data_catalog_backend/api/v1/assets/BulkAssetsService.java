package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostAssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;

/**
 * @author juliwolf
 */

@Service
public class BulkAssetsService {
  private final LanguageService languageService;

  private final AssetsDAO assetsDAO;

  public BulkAssetsService (
    LanguageService languageService,
    AssetsDAO assetsDAO
  ) {
    this.languageService = languageService;
    this.assetsDAO = assetsDAO;
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public List<PostAssetResponse> createAssets (
    List<PostOrPatchAssetRequest> assetsRequest,
    Map<UUID, AssetType> assetTypeMap,
    Map<UUID, Status> lifecycleStatusMap,
    Map<UUID, Status> stewardshipStatusMap,
    User user
  ) throws DataIntegrityViolationException {
    Language language = languageService.getLanguage("ru");

    return assetsRequest.stream()
      .map(request -> {
          AssetType assetType = assetTypeMap.get(UUID.fromString(request.getAsset_type_id()));

          Status lifecycleStatus = null;
          if (StringUtils.isNotEmpty(request.getLifecycle_status())) {
            lifecycleStatus = lifecycleStatusMap.get(UUID.fromString(request.getLifecycle_status()));
          }

          Status stewardshipStatus = null;
          if (StringUtils.isNotEmpty(request.getStewardship_status())) {
            stewardshipStatus = stewardshipStatusMap.get(UUID.fromString(request.getStewardship_status()));
          }

          Asset asset = assetsDAO.assetRepository.save(new Asset(
            request.getAsset_name(),
            assetType,
            request.getAsset_displayname(),
            language,
            lifecycleStatus,
            stewardshipStatus,
            user
          ));

          assetsDAO.createAssetHistory(asset, MethodType.POST);

          return new PostAssetResponse(
            asset.getAssetId(),
            asset.getAssetName(),
            asset.getAssetDisplayName(),
            asset.getAssetType().getAssetTypeId(),
            assetType.getAssetTypeName(),
            lifecycleStatus != null ? lifecycleStatus.getStatusId() : null,
            lifecycleStatus != null ? lifecycleStatus.getStatusName() : null,
            stewardshipStatus != null ? stewardshipStatus.getStatusId() : null,
            stewardshipStatus != null ? stewardshipStatus.getStatusName() : null,
            language.getLanguage(),
            asset.getCreatedOn(),
            user.getUserId()
          );
        }
      ).toList();
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public List<AssetResponse> updateAssets (
    List<PatchAssetRequest> assetsRequest,
    Map<UUID, Asset> assetsMap,
    Map<UUID, AssetType> assetTypeMap,
    Map<UUID, Status> lifecycleStatusMap,
    Map<UUID, Status> stewardshipStatusMap,
    User user
  ) {
    return assetsRequest.stream()
      .map(request -> {
        Asset foundAsset = assetsMap.get(request.getAsset_id());

        AssetType assetType = null;
        if (StringUtils.isNotEmpty(request.getAsset_type_id())) {
          assetType = assetTypeMap.get(UUID.fromString(request.getAsset_type_id()));
          foundAsset.setAssetType(assetType);
        }

        if (StringUtils.isNotEmpty(request.getAsset_name())) {
          foundAsset.setAssetName(request.getAsset_name());
        }

        if (StringUtils.isNotEmpty(request.getAsset_displayname())) {
          foundAsset.setAssetDisplayName(request.getAsset_displayname());
        }

        Status lifecycleStatus = null;
        if (request.getLifecycle_status() != null) {
          lifecycleStatus = lifecycleStatusMap.get(request.getLifecycle_status());
          foundAsset.setLifecycleStatus(lifecycleStatus);
        }

        Status stewardhipStatus = null;
        if (request.getStewardship_status() != null) {
          stewardhipStatus = stewardshipStatusMap.get(request.getStewardship_status());
          foundAsset.setStewardshipStatus(stewardhipStatus);
        }

        assetsDAO.validateAssetNameByAssetTypeValidationMask(
          foundAsset.getAssetType().getAssetNameValidationMask(),
          request.getAsset_name(),
          foundAsset.getAssetType().getAssetNameValidationMaskExample()
          );

        foundAsset.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
        foundAsset.setModifiedBy(user);

        assetsDAO.assetRepository.save(foundAsset);
        assetsDAO.createAssetHistory(foundAsset, MethodType.PATCH);

        boolean hasAssetType = assetType != null;
        boolean hasLifecycleStatus = lifecycleStatus != null;
        boolean hasStewardshipStatus = stewardhipStatus != null;

        return new AssetResponse(
          foundAsset.getAssetId(),
          foundAsset.getAssetName(),
          foundAsset.getAssetDisplayName(),
          hasAssetType ? assetType.getAssetTypeId() : null,
          hasAssetType ? assetType.getAssetTypeName() : null,
          hasLifecycleStatus ? foundAsset.getLifecycleStatus().getStatusId() : null,
          hasLifecycleStatus ? foundAsset.getLifecycleStatus().getStatusName() : null,
          hasStewardshipStatus ? foundAsset.getStewardshipStatus().getStatusId() : null,
          hasStewardshipStatus ? foundAsset.getStewardshipStatus().getStatusName() : null,
          foundAsset.getLanguageName(),
          foundAsset.getCreatedOn(),
          foundAsset.getCreatedByUUID(),
          foundAsset.getLastModifiedOn(),
          user.getUserId()
        );
      }).toList();
  }
}
