package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.AttributeValueValidator;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeIdAttributeTypeIdAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeHistory.AttributeHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models.AttributeWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.CollectionUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetLinkUsage.AssetLinkUsageDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.AssetTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AssetAlreadyHasAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeTypeNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.linkUsage.LinkUsageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.AttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.get.GetAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.imageLinkUsage.ImageLinkUsageDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;

/**
 * @author JuliWolf
 */
@Service
public class AttributesServiceImpl extends AttributesDAO implements AttributesService {
  private final Integer PAGE_SIZE = 100;

  private final AssetsDAO assetsDAO;

  private final AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO;

  private final AttributeTypesDAO attributeTypesDAO;

  private final AttributeValueValidator attributeValueValidator;

  private final LanguageService languageService;

  private final LinkUsageService linkUsageService;

  private final BulkAttributesService bulkAttributesService;

  @Autowired
  public AttributesServiceImpl (
    AttributeRepository attributeRepository,
    AttributeHistoryRepository attributeHistoryRepository,
    AssetLinkUsageDAO assetLinkUsageDAO,
    ImageLinkUsageDAO imageLinkUsageDAO,
    AssetsDAO assetsDAO,
    AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO,
    AttributeTypesDAO attributeTypesDAO,
    AttributeValueValidator attributeValueValidator,
    LanguageService languageService,
    LinkUsageService linkUsageService,
    BulkAttributesService bulkAttributesService
  ) {
    super(
      attributeRepository,
      attributeHistoryRepository,
      assetLinkUsageDAO,
      imageLinkUsageDAO
    );

    this.assetsDAO = assetsDAO;
    this.assetTypeAttributeTypesAssignmentsDAO = assetTypeAttributeTypesAssignmentsDAO;
    this.attributeTypesDAO = attributeTypesDAO;
    this.attributeValueValidator = attributeValueValidator;
    this.languageService = languageService;
    this.linkUsageService = linkUsageService;
    this.bulkAttributesService = bulkAttributesService;
  }

  @Override
  @Transactional
  public PostAttributeResponse createAttribute (
    PostAttributeRequest attributeRequest,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    AttributeTypeNotAllowedException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException
  {
    LoggerWrapper.info("Starting creating attribute", AttributesServiceImpl.class.getName());

    Asset asset = assetsDAO.findAssetById(attributeRequest.getAsset_id());
    UUID attributeTypeId = UUID.fromString(attributeRequest.getAttribute_type_id());

    Boolean isAssignmentsExists = assetTypeAttributeTypesAssignmentsDAO.isAssignmentsExistingByAssetTypeIdAndAttributeTypeId(asset.getAssetType().getAssetTypeId(), attributeTypeId);
    if (!isAssignmentsExists) {
      throw new AttributeTypeNotAllowedException();
    }

    AttributeType attributeType = attributeTypesDAO.findAttributeTypeById(attributeTypeId, false);
    attributeValueValidator.validateValueType(attributeRequest.getValue(), attributeType.getAttributeTypeId(), attributeType.getValidationMask(), attributeType.getAttributeKindType());

    Language ru = languageService.getLanguage("ru");
    Attribute attribute = new Attribute(
      attributeType,
      asset,
      ru,
      user
    );

    attributeValueValidator.setAttributeValueByType(attribute, attributeRequest.getValue(), attributeType.getAttributeKindType());

    if (attributeType.getAttributeKindType().equals(AttributeKindType.RTF)) {
      linkUsageService.parseAttributeValueToAttributeLink(attribute, user);
      linkUsageService.parseAttributeValueToImageLink(attribute, user);
    }

    attribute = attributeRepository.save(attribute);
    createAttributeHistory(attribute, MethodType.POST);

    return new PostAttributeResponse(
      attribute.getAttributeId(),
      attributeType.getAttributeTypeId(),
      asset.getAssetId(),
      attribute.getValue(),
      attribute.getIsInteger(),
      attribute.getValueNumeric(),
      attribute.getValueBoolean(),
      attribute.getValueDatetime(),
      ru.getLanguage(),
      attribute.getCreatedOn(),
      user.getUserId()
    );
  }

  @Override
  public List<PostAttributeResponse> createAttributesBulk (
    List<PostAttributeRequest> attributeRequests,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    DuplicateValueInRequestException,
    AttributeTypeNotAllowedException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    SomeRequiredFieldsAreEmptyException,
    AttributeValueMaskValidationException
  {
    LoggerWrapper.info("Starting creating attributes bulk", AttributesServiceImpl.class.getName());

    Set<UUID> assetsSet = new HashSet<>();
    Set<UUID> attributeTypesSet = new HashSet<>();
    Map<UUID, UUID> attributeTypeAssetMap = new HashMap<>();
    Map<AbstractMap.SimpleEntry<UUID, UUID>, Integer> attributeTypeAssetDuplicatesCountMap = new HashMap<>();

    attributeRequests.forEach(request -> {
      if (
        request.getAsset_id() == null ||
        StringUtils.isEmpty(request.getValue()) ||
        StringUtils.isEmpty(request.getAttribute_type_id())
      ) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      UUID assetId = request.getAsset_id();
      UUID attributeTypeId = UUID.fromString(request.getAttribute_type_id());

      assetsSet.add(assetId);
      attributeTypesSet.add(attributeTypeId);

      attributeTypeAssetMap.put(attributeTypeId, assetId);
      ObjectUtils.putAndComputeKeys(attributeTypeAssetDuplicatesCountMap, new AbstractMap.SimpleEntry<>(attributeTypeId, assetId));
    });

    checkDuplicatesInBulkRequest(attributeTypeAssetDuplicatesCountMap);

    Map<UUID, Asset> assetsMap = findAssets(assetsSet);
    Map<UUID, AttributeType> attributeTypesMap = findAttributeTypes(attributeTypesSet);

    checkIfAssetTypeAttributeTypeAssignmentsExists(attributeTypeAssetMap, assetsMap);

    try {
      return bulkAttributesService.createAttributes(attributeRequests, assetsMap, attributeTypesMap, user);
    } catch (DataIntegrityViolationException exception) {
      throw new AssetAlreadyHasAttributeException(exception);
    }
  }

  @Override
  @Transactional
  public PatchAttributeResponse updateAttribute (
    UUID attributeId,
    PatchAttributeRequest attributeRequest,
    User user
  ) throws
    AttributeNotFoundException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException
  {
    LoggerWrapper.info("Starting updating attribute " + attributeId, AttributesServiceImpl.class.getName());

    Attribute attribute = findAttributeById(attributeId);
    AttributeType attributeType = attribute.getAttributeType();

    return updateAttribute(attributeRequest.getValue(), attribute, attributeType, user);
  }

  @Override
  @Transactional
  public List<PatchAttributeResponse> updateAttributesBulk (
    List<PatchBulkAttributeRequest> attributesRequest,
    User user
  ) throws
    AttributeNotFoundException,
    DuplicateValueInRequestException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    SomeRequiredFieldsAreEmptyException,
    AttributeValueMaskValidationException
  {
    LoggerWrapper.info("Starting updating attributes bulk", AttributesServiceImpl.class.getName());

    Set<UUID> attributesSet = new HashSet<>();

    attributesRequest.forEach(request -> {
      if (request.getAttribute_id() == null || StringUtils.isEmpty(request.getValue())) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      if (attributesSet.contains(request.getAttribute_id())) {
        throw new DuplicateValueInRequestException("Several entries for this attribute_id", ObjectUtils.convertObjectToMap(request));
      }

      attributesSet.add(request.getAttribute_id());
    });

    Map<UUID, Attribute> attributes = findAttributes(attributesSet);
    List<AttributeType> attributeTypes = attributeTypesDAO.findAttributeTypesByAttributeIds(attributesSet.stream().toList());
    Map<UUID, AttributeType> attrbutesTypesMap = attributeTypes.stream().collect(Collectors.toMap(AttributeType::getAttributeTypeId, value -> value));

    return attributesRequest.stream()
      .map(attributeRequest -> {
        Attribute attribute = attributes.get(attributeRequest.getAttribute_id());
        AttributeType attributeType = attrbutesTypesMap.get(attribute.getAttributeType().getAttributeTypeId());

        return updateAttribute(attributeRequest.getValue(), attribute, attributeType, user);
      }).toList();
  }

  @Override
  public AttributeResponse getAttributeById (UUID attributeId) throws AttributeNotFoundException {
    LoggerWrapper.info("Starting getting attribute by id " + attributeId, AttributesServiceImpl.class.getName());

    Optional<AttributeWithConnectedValues> optionalAttribute = attributeRepository.findAttributeByIdWithJoinedTables(attributeId);

    if (optionalAttribute.isEmpty()) {
      throw new AttributeNotFoundException(attributeId);
    }

    AttributeWithConnectedValues attribute = optionalAttribute.get();

    return new AttributeResponse(
      attribute.getAttributeId(),
      attribute.getAttributeTypeId(),
      attribute.getAttributeTypeName(),
      attribute.getAttributeKind(),
      attribute.getAssetId(),
      attribute.getAssetDisplayName(),
      attribute.getAssetFullName(),
      attribute.getValue(),
      attribute.getIsInteger(),
      attribute.getValueNumeric(),
      attribute.getValueBoolean(),
      attribute.getValueDatetime(),
      attribute.getLanguageName(),
      attribute.getCreatedOn(),
      attribute.getCreatedBy(),
      attribute.getLastModifiedOn(),
      attribute.getLastModifiedBy()
    );
  }

  @Override
  public GetAttributesResponse getAttributesByParams (
    UUID assetId,
    UUID attributeTypeId,
    Integer pageNumber,
    Integer pageSize
  ) {
    LoggerWrapper.info("Starting getting attributes by params", AttributesServiceImpl.class.getName());

    pageSize = PageableUtils.getPageSize(pageSize, PAGE_SIZE);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AttributeWithConnectedValues> attributes = attributeRepository.findAllByParamsWithJoinedTablesPageable(
      assetId,
      attributeTypeId,
      PageRequest.of(pageNumber, pageSize, Sort.by("at.attributeTypeName").ascending())
    );

    List<AttributeResponse> attributesList = attributes
      .stream()
      .map(attribute -> new AttributeResponse(
        attribute.getAttributeId(),
        attribute.getAttributeTypeId(),
        attribute.getAttributeTypeName(),
        attribute.getAttributeKind(),
        attribute.getAssetId(),
        attribute.getAssetDisplayName(),
        attribute.getAssetFullName(),
        attribute.getValue(),
        attribute.getIsInteger(),
        attribute.getValueNumeric(),
        attribute.getValueBoolean(),
        attribute.getValueDatetime(),
        attribute.getLanguageName(),
        attribute.getCreatedOn(),
        attribute.getCreatedBy(),
        attribute.getLastModifiedOn(),
        attribute.getLastModifiedBy()
      ))
      .toList();

    return new GetAttributesResponse(
      attributes.getTotalElements(),
      pageSize,
      pageNumber,
      attributesList
    );
  }

  @Override
  @Transactional
  public void deleteAttributeById (UUID attributeId, User user) throws AttributeNotFoundException {
    LoggerWrapper.info("Starting deleting attributes by id " + attributeId, AttributesServiceImpl.class.getName());

    Attribute foundAttribute = findAttributeById(attributeId);

    clearLinks(List.of(attributeId), user);

    foundAttribute.setIsDeleted(true);
    foundAttribute.setDeletedBy(user);
    foundAttribute.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    attributeRepository.save(foundAttribute);
    createAttributeHistory(foundAttribute, MethodType.DELETE);
  }

  @Override
  @Transactional
  public void deleteAttributesBulk (List<UUID> attributeRequests, User user) throws AttributeNotFoundException {
    LoggerWrapper.info("Starting deleting attributes bulk", AttributesServiceImpl.class.getName());

    UUID firstDuplicate = CollectionUtils.findFirstDuplicate(attributeRequests);
    if (firstDuplicate != null) {
      throw new DuplicateValueInRequestException("Duplicate attribute_id in request", Map.of("attribute_id", firstDuplicate));
    }

    List<UUID> attributeIds = attributeRepository.findIdsByAttributeIds(attributeRequests);

    if (attributeIds.size() != attributeRequests.size()) {
      UUID attributeId = CollectionUtils.findFirstNotFoundValue(
        new HashSet<>(attributeRequests),
        new HashSet<>(attributeIds)
      );

      throw new AttributeNotFoundException(Map.of("attribute_id", attributeId));
    }

    attributeRepository.deleteAllByAttributeIds(attributeRequests, user.getUserId());
    attributeRequests.forEach(attributeId -> {
      attributeHistoryRepository.createAttributeHistoryFromAttribute(UUID.randomUUID(), attributeId);
      attributeHistoryRepository.updateLastAttributeHistoryByDeletedAttributeId(attributeId, HistoryDateUtils.getValidToDefaultTime());
    });

    clearLinks(attributeRequests, user);
  }

  private Map<UUID, Asset> findAssets (Set<UUID> assetsSet) throws AssetNotFoundException {
    List<Asset> assetsList = assetsDAO.findAllByAssetIds(assetsSet.stream().toList());

    Map<UUID, Asset> assetsMap = assetsList.stream().collect(Collectors.toMap(Asset::getAssetId, value -> value));

    if (assetsList.size() != assetsSet.size()) {
      UUID absentAssetId = CollectionUtils.findFirstNotFoundValue(assetsSet, assetsMap.keySet());

      PostAttributeRequest request = new PostAttributeRequest();
      request.setAsset_id(absentAssetId);

      throw new AssetNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return assetsMap;
  }

  private Map<UUID, Attribute> findAttributes (Set<UUID> attributesSet) throws AttributeNotFoundException {
    List<Attribute> attributesList = attributeRepository.findAllByAttributeIds(attributesSet.stream().toList());

    Map<UUID, Attribute> attrbutesMap = attributesList.stream().collect(Collectors.toMap(Attribute::getAttributeId, value -> value));

    if (attributesList.size() != attributesSet.size()) {
      UUID absentAttributeId = CollectionUtils.findFirstNotFoundValue(attributesSet, attrbutesMap.keySet());

      PostAttributeRequest request = new PostAttributeRequest();
      request.setAsset_id(absentAttributeId);

      throw new AttributeNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return attrbutesMap;
  }

  private Map<UUID, AttributeType> findAttributeTypes (Set<UUID> attributeTypesSet) throws AttributeTypeNotFoundException {
    List<AttributeType> attributeTypesList = attributeTypesDAO.findAttributeTypesByIds(attributeTypesSet.stream().toList());

    Map<UUID, AttributeType> attributeTypesMap = attributeTypesList.stream().collect(Collectors.toMap(AttributeType::getAttributeTypeId, value -> value));

    if (attributeTypesList.size() != attributeTypesSet.size()) {
      UUID absentAttributeTypeId = CollectionUtils.findFirstNotFoundValue(attributeTypesSet, attributeTypesMap.keySet());

      PostAttributeRequest request = new PostAttributeRequest();
      request.setAttribute_type_id(absentAttributeTypeId.toString());

      throw new AttributeTypeNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return attributeTypesMap;
  }

  private PatchAttributeResponse updateAttribute (
    String value,
    Attribute attribute,
    AttributeType attributeType,
    User user
  ) throws
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException
  {
    attributeValueValidator.validateValueType(value, attributeType.getAttributeTypeId(), attributeType.getValidationMask(), attributeType.getAttributeKindType());

    attributeValueValidator.setAttributeValueByType(attribute, value, attributeType.getAttributeKindType());

    updateAssetLinkUsage(attribute, attributeType, user);

    attribute.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    attribute.setModifiedBy(user);

    attribute = attributeRepository.save(attribute);
    createAttributeHistory(attribute, MethodType.PATCH);

    return new PatchAttributeResponse(
      attribute.getAttributeId(),
      attributeType.getAttributeTypeId(),
      attribute.getAsset().getAssetId(),
      attribute.getValue(),
      attribute.getIsInteger(),
      attribute.getValueNumeric(),
      attribute.getValueBoolean(),
      attribute.getValueDatetime(),
      attribute.getLanguageName(),
      attribute.getCreatedOn(),
      attribute.getCreatedByUUID(),
      attribute.getLastModifiedOn(),
      user.getUserId()
    );
  }

  private void checkDuplicatesInBulkRequest (Map<AbstractMap.SimpleEntry<UUID, UUID>, Integer> countMap) throws DuplicateValueInRequestException {
    countMap.entrySet().stream()
      .filter(entry -> entry.getValue() > 1)
      .limit(1)
      .forEach(entry -> {
        PostAttributeRequest request = new PostAttributeRequest();
        request.setAttribute_type_id(entry.getKey().getKey().toString());
        request.setAsset_id(entry.getKey().getValue());

        throw new DuplicateValueInRequestException("Duplicate in request", ObjectUtils.convertObjectToMap(request));
      });
  }

  private void checkIfAssetTypeAttributeTypeAssignmentsExists (
    Map<UUID, UUID> attributeTypeAssetMap,
    Map<UUID, Asset> assetsMap
  ) throws AttributeTypeNotAllowedException {
    Set<AbstractMap.SimpleEntry<UUID, UUID>> attributeTypeAssetTypeSet = new HashSet<>();
    List<UUID> assetTypeIds = new ArrayList<>();
    List<UUID> attributeTypeIds = new ArrayList<>();

    attributeTypeAssetMap
      .forEach((key, value) -> {
        Asset asset = assetsMap.get(value);

        attributeTypeIds.add(key);
        assetTypeIds.add(asset.getAssetType().getAssetTypeId());

        attributeTypeAssetTypeSet.add(new AbstractMap.SimpleEntry<>(key, asset.getAssetType().getAssetTypeId()));
      });

    List<AssetTypeIdAttributeTypeIdAssignment> assignments = assetTypeAttributeTypesAssignmentsDAO.findAllAssetTypeAttributeTypeAssignmentsByAttributeTypeIdsAndAssetIds(attributeTypeIds, assetTypeIds);

    if (attributeTypeAssetTypeSet.size() == assignments.size()) return;

    Set<AbstractMap.SimpleEntry<UUID, UUID>> assingmnetsSet = assignments.stream()
      .map(assignment -> new AbstractMap.SimpleEntry<>(assignment.getAttributeTypeId(), assignment.getAssetTypeId()))
      .collect(Collectors.toSet());

    attributeTypeAssetTypeSet.forEach(value -> {
      if (!assingmnetsSet.contains(value)) {
        UUID assetId = attributeTypeAssetMap.get(value.getKey());

        PostAttributeRequest request = new PostAttributeRequest();
        request.setAsset_id(assetId);
        request.setAttribute_type_id(value.getKey().toString());

        throw new AttributeTypeNotAllowedException(ObjectUtils.convertObjectToMap(request));
      }
    });
  }

  private void updateAssetLinkUsage (Attribute attribute, AttributeType attributeType, User user) {
    clearLinks(List.of(attribute.getAttributeId()), user);

    if (!attributeType.getAttributeKindType().equals(AttributeKindType.RTF)) return;

    linkUsageService.parseAttributeValueToAttributeLink(attribute, user);
    linkUsageService.parseAttributeValueToImageLink(attribute, user);
  }
}
