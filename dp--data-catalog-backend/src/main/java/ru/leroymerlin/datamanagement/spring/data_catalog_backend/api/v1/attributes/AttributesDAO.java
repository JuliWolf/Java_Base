package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetIdResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeHistory.AttributeHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models.ReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetLinkUsage.AssetLinkUsageDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.imageLinkUsage.ImageLinkUsageDAO;

@Service
public class AttributesDAO {
  protected final AttributeRepository attributeRepository;

  protected final AttributeHistoryRepository attributeHistoryRepository;

  protected final AssetLinkUsageDAO assetLinkUsageDAO;

  protected final ImageLinkUsageDAO imageLinkUsageDAO;

  public AttributesDAO (
    AttributeRepository attributeRepository,
    AttributeHistoryRepository attributeHistoryRepository,
    AssetLinkUsageDAO assetLinkUsageDAO,
    ImageLinkUsageDAO imageLinkUsageDAO
  ) {
    this.attributeRepository = attributeRepository;
    this.attributeHistoryRepository = attributeHistoryRepository;
    this.assetLinkUsageDAO = assetLinkUsageDAO;
    this.imageLinkUsageDAO = imageLinkUsageDAO;
  }

  public Attribute findAttributeById (UUID attributeId) throws AttributeNotFoundException {
    Optional<Attribute> attribute = attributeRepository.findById(attributeId);

    if (attribute.isEmpty()) {
      throw new AttributeNotFoundException(attributeId);
    }

    if (attribute.get().getIsDeleted()) {
      throw new AttributeNotFoundException(attributeId);
    }

    return attribute.get();
  }

  public void flush () {
    attributeRepository.flush();
  }

  public Attribute saveAttribute (Attribute attribute) {
    return attributeRepository.save(attribute);
  }

  public Boolean isAttributesExistsByAttributeType (UUID attributeTypeId) {
    return attributeRepository.isAttributeExistsByAttributeTypeId(attributeTypeId);
  }

  public UUID findFirstAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue (UUID attributeTypeId, String value) {
    return attributeRepository.findFirstAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue(attributeTypeId, value);
  }

  public UUID findFirstAssetIdByAssetTypeIdAndAttributeTypeId (UUID assetTypeId, UUID attributeTypeId) {
    List<AssetIdResponse> assetIdResponseList = attributeRepository.findFirstAssetIdByAssetTypeIdAndAttributeTypeId(assetTypeId, attributeTypeId);

    if (assetIdResponseList.isEmpty()) {
      return null;
    }

    return assetIdResponseList.get(0).getAssetId();
  }

  public List<Attribute> findAllByAttributeIds (List<UUID> attributeIds) {
    return attributeRepository.findAllByAttributeIds(attributeIds);
  }

  public Page<ReportReviewRequest> getReportReviewRequestsByParamsPageable (
    String reviewerLdap,
    String reportDataCatalogId,
    Pageable pageable
  ) {
    return attributeRepository.getReportReviewRequestsByParamsPageable(reviewerLdap, reportDataCatalogId, pageable);
  }

  public Optional<ReportReviewRequest> getReportReviewRequestByReportId (String reportReviewRequestId) {
    return attributeRepository.getReportReviewRequestByReportId(reportReviewRequestId);
  }

  public Boolean isReportReviewExistsByReportId (String reportReviewRequestId) {
    return attributeRepository.isReportReviewExistsByReportId(reportReviewRequestId);
  }

  public Optional<Attribute> findReportRequestByReportReviewId (String reportReviewId) {
    return attributeRepository.findReportRequestByReportReviewId(reportReviewId);
  }

  public Optional<Attribute> findFirstAttributeByParams (UUID assetId, UUID attributeTypeId) {
    return attributeRepository.findFirstAttributeByParams(assetId, attributeTypeId);
  }

  public void deleteAllByAttributeIds (List<UUID> attributeIds, User user) {
    attributeRepository.deleteAllByAttributeIds(attributeIds, user.getUserId());
  }

  public void deleteAllByAssetIds (List<UUID> assetIds, User user) {
    attributeRepository.deleteAllByAssetIds(assetIds, user.getUserId());
  }

  // TODO: return to private method inside Service after deleting reports methods
  public void clearLinks (List<UUID> attributeIds, User user) {
    assetLinkUsageDAO.deleteAssetLinkByAttributeId(attributeIds, user);
    imageLinkUsageDAO.deleteImagesLinkByAttributeId(attributeIds, user);
  }

  public void createAttributeHistory (Attribute attribute, MethodType methodType) {
    AttributeHistory attributeHistory = new AttributeHistory(attribute);

    switch (methodType) {
      case POST -> attributeHistory.setCreatedValidDate();
      case PATCH -> {
        attributeHistory.setUpdatedValidDate();
        attributeHistoryRepository.updateLastAttributeHierarchy(
          attribute.getLastModifiedOn(),
          attribute.getAttributeId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
      case DELETE -> {
        attributeHistory.setDeletedValidDate();

        attributeHistoryRepository.updateLastAttributeHierarchy(
          attribute.getDeletedOn(),
          attribute.getAttributeId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
    }

    attributeHistoryRepository.save(attributeHistory);
  }
}
