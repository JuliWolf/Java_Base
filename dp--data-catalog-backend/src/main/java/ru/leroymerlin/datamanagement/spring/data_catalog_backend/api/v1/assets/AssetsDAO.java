package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHistory.AssetHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetIdResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetReport;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetHistory;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.ReviewStatus;

@Service
public class AssetsDAO {
  protected final AssetRepository assetRepository;

  protected final AssetHistoryRepository assetHistoryRepository;

  public AssetsDAO (
    AssetRepository assetRepository,
    AssetHistoryRepository assetHistoryRepository
  ) {
    this.assetRepository = assetRepository;
    this.assetHistoryRepository = assetHistoryRepository;
  }

  public Asset findAssetById(UUID assetId) throws AssetNotFoundException {
    Optional<Asset> asset = assetRepository.findById(assetId);

    if (asset.isEmpty()) {
      throw new AssetNotFoundException();
    }

    if (asset.get().getIsDeleted()) {
      throw new AssetNotFoundException();
    }

    return asset.get();
  }

  public UUID findFirstAssetByAssetTypeAndStatusId (UUID statusId, UUID assetTypeId) {
    List<AssetIdResponse> assetIdResponseList = assetRepository.findFirstAssetByAssetTypeAndStatusId(statusId, assetTypeId);

    if (assetIdResponseList.isEmpty()) {
      return null;
    }

    return assetIdResponseList.get(0).getAssetId();
  }

  public List<Asset> findAllByAssetIds (List<UUID> assetIds) {
    return assetRepository.findAllByAssetIds(assetIds);
  }

  public Page<AssetReport> getReportsByParamsPageable (
    String reviewStatus,
    Boolean hasDataCatalogDescription,
    String reportName,
    Long uniqueUsersLastMonthMin,
    Long uniqueUsersLastMonthMax,
    java.sql.Timestamp reportLastModifiedDateMin,
    java.sql.Timestamp reportLastModifiedDateMax,
    String reportConfidentiality,
    Pageable pageable
  ) {
    reportLastModifiedDateMin = reportLastModifiedDateMin != null ? reportLastModifiedDateMin : new Timestamp(0);
    reportLastModifiedDateMax = reportLastModifiedDateMax != null ? reportLastModifiedDateMax : new java.sql.Timestamp(Long.MAX_VALUE / 1000);

    return assetRepository.getReportsByParamsPageable(
      reviewStatus,
      hasDataCatalogDescription,
      reportName,
      uniqueUsersLastMonthMin,
      uniqueUsersLastMonthMax,
      reportLastModifiedDateMin,
      reportLastModifiedDateMax,
      reportConfidentiality,
      pageable
    );
  }

  public Optional<AssetReport> getReportsById (UUID reportId) {
    return assetRepository.getReportsById(reportId);
  }

  public Optional<Asset> findAssetByReportId (UUID reportId) {
    return assetRepository.findAssetByReportId(reportId);
  }

  public Boolean isReportExistById (UUID reportId) {
    return assetRepository.isReportExistById(reportId);
  }

  public void updateReportReviewStatus (UUID reportId, ReviewStatus reviewStatus) {
    assetRepository.updateReportReviewStatus(reportId, reviewStatus.getValue());
  }

  public void createAssetHistory (Asset asset, MethodType methodType) {
    AssetHistory assetHistory = new AssetHistory(asset);

    switch (methodType) {
      case POST -> assetHistory.setCreatedValidDate();
      case PATCH -> {
        assetHistory.setUpdatedValidDate();
        assetHistoryRepository.updateLastAssetHistory(
          asset.getLastModifiedOn(),
          asset.getAssetId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
      case DELETE -> {
        assetHistory.setDeletedValidDate();

        assetHistoryRepository.updateLastAssetHistory(
          asset.getDeletedOn(),
          asset.getAssetId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
    }

    assetHistoryRepository.save(assetHistory);
  }

  public void validateAssetNameByAssetTypeValidationMask (
    String assetNameValidationMask,
    String assetName,
    String assetNameValidationMaskExample
  ) {
    if (StringUtils.isEmpty(assetNameValidationMask)) return;

    if (StringUtils.isEmpty(assetName)) return;

    boolean isValid = Pattern.matches(assetNameValidationMask, assetName);

    if (!isValid) {
      throw new AssetNameDoesNotMatchPatternException(assetNameValidationMaskExample);
    }
  }
}
