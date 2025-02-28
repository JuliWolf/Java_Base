package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import logger.LoggerWrapper;
import org.hibernate.transform.Transformers;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHistory.AssetHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.models.AssetTypeCardHeaderAssignmentResponsible;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.CollectionUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.SortUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetLinkUsage.AssetLinkUsageDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.dto.AssetChangeHistoryDTO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.dto.AssetChangeHistoryResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.ChildrenSortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.AssetTypeStatusesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.StatusesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.SubscriptionsDAO;

/**
 * @author JuliWolf
 */
@Service
public class AssetsServiceImpl extends AssetsDAO implements AssetsService {
  private final RoleActionCachingService roleActionCachingService;

  private final AssetTypesDAO assetTypesDAO;

  private final AssetTypeStatusesAssignmentsDAO assetTypeStatusesAssignmentsDAO;

  private final LanguageService languageService;

  private final StatusesDAO statusesDAO;

  private final ResponsibilitiesDAO responsibilitiesDAO;

  private final RelationsDAO relationsDAO;

  private final AssetLinkUsageDAO assetLinkUsageDAO;

  private final AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO;

  private final SubscriptionsDAO subscriptionsDAO;

  private final AttributesDAO attributesDAO;

  private final BulkAssetsService bulkAssetsService;

  private final EntityManager entityManager;

  @Autowired
  public AssetsServiceImpl (
    StatusesDAO statusesDAO,
    RelationsDAO relationsDAO,
    AttributesDAO attributesDAO,
    AssetTypesDAO assetTypesDAO,
    LanguageService languageService,
    SubscriptionsDAO subscriptionsDAO,
    AssetLinkUsageDAO assetLinkUsageDAO,
    ResponsibilitiesDAO responsibilitiesDAO,
    RoleActionCachingService roleActionCachingService,
    AssetTypeStatusesAssignmentsDAO assetTypeStatusesAssignmentsDAO,
    AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO,
    BulkAssetsService bulkAssetsService,
    AssetRepository assetRepository,
    AssetHistoryRepository assetHistoryRepository,
    EntityManager entityManager
  ) {
    super(assetRepository, assetHistoryRepository);

    this.roleActionCachingService = roleActionCachingService;
    this.assetTypesDAO = assetTypesDAO;
    this.assetTypeStatusesAssignmentsDAO = assetTypeStatusesAssignmentsDAO;
    this.languageService = languageService;
    this.statusesDAO = statusesDAO;
    this.responsibilitiesDAO = responsibilitiesDAO;
    this.relationsDAO = relationsDAO;
    this.assetLinkUsageDAO = assetLinkUsageDAO;
    this.assetTypeCardHeaderAssignmentDAO = assetTypeCardHeaderAssignmentDAO;
    this.subscriptionsDAO = subscriptionsDAO;
    this.attributesDAO = attributesDAO;
    this.bulkAssetsService = bulkAssetsService;
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public PostAssetResponse createAsset (
    PostOrPatchAssetRequest assetRequest,
    User user
  ) throws
    StatusNotFoundException,
    IllegalArgumentException,
    AssetTypeNotFoundException,
    AssetNameDoesNotMatchPatternException,
    AssetTypeStatusAssignmentNotFoundException
  {
    LoggerWrapper.info("Starting create asset", AssetsServiceImpl.class.getName());

    AssetType assetType = assetTypesDAO.findAssetTypeById(UUID.fromString(assetRequest.getAsset_type_id()));
    Status lifecycleStatus = null;
    Status stewardshipStatus = null;

    if (StringUtils.isNotEmpty(assetRequest.getLifecycle_status())) {
      lifecycleStatus = getStatus(assetRequest.getLifecycle_status(), AssignmentStatusType.LIFECYCLE);
    }

    if (StringUtils.isNotEmpty(assetRequest.getStewardship_status())) {
      stewardshipStatus = getStatus(assetRequest.getStewardship_status(), AssignmentStatusType.STEWARDSHIP);
    }

    validateAssetNameByAssetTypeValidationMask(assetType.getAssetNameValidationMask(), assetRequest.getAsset_name(), assetType.getAssetNameValidationMaskExample());

    Language language = languageService.getLanguage("ru");
    Asset asset = assetRepository.save(new Asset(
      assetRequest.getAsset_name(),
      assetType,
      assetRequest.getAsset_displayname(),
      language,
      lifecycleStatus,
      stewardshipStatus,
      user
    ));

    createAssetHistory(asset, MethodType.POST);

    return new PostAssetResponse(
      asset.getAssetId(),
      asset.getAssetName(),
      asset.getAssetDisplayName(),
      assetType.getAssetTypeId(),
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

  @Override
  public List<PostAssetResponse> createAssetsBulk (
    List<PostOrPatchAssetRequest> assetsRequest,
    User user
  ) throws
    StatusNotFoundException,
    IllegalArgumentException,
    AssetTypeNotFoundException,
    InvalidFieldLengthException,
    AssetNameAlreadyExistsException,
    DuplicateValueInRequestException,
    SomeRequiredFieldsAreEmptyException,
    AssetTypeStatusAssignmentNotFoundException
  {
    LoggerWrapper.info("Starting create bulk asset", AssetsServiceImpl.class.getName());

    Set<UUID> assetTypeSet = new HashSet<>();
    Set<UUID> lifecycleStatusSet = new HashSet<>();
    Set<UUID> stewardshipStatusSet = new HashSet<>();
    Map<String, Integer> requestedAssetNameMap = new HashMap<>();

    assetsRequest.forEach(request -> {
      if (
        StringUtils.isEmpty(request.getAsset_name()) ||
        StringUtils.isEmpty(request.getAsset_type_id()) ||
        StringUtils.isEmpty(request.getAsset_displayname())
      ) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      if (request.getAsset_name().length() > 255) {
        throw new InvalidFieldLengthException(ObjectUtils.convertObjectToMap(request), "asset_name", 255);
      }

      assetTypeSet.add(UUID.fromString(request.getAsset_type_id()));

      if (StringUtils.isNotEmpty(request.getLifecycle_status())) {
        lifecycleStatusSet.add(UUID.fromString(request.getLifecycle_status()));
      }

      if (StringUtils.isNotEmpty(request.getStewardship_status())) {
        stewardshipStatusSet.add(UUID.fromString(request.getStewardship_status()));
      }

      ObjectUtils.putAndComputeKeys(requestedAssetNameMap, request.getAsset_name());
    });

    requestedAssetNameMap.entrySet().stream()
      .filter(entry -> entry.getValue() > 1)
      .limit(1)
      .forEach(entry -> {
        PostOrPatchAssetRequest request = new PostOrPatchAssetRequest();
        request.setAsset_name(entry.getKey());

        throw new DuplicateValueInRequestException("Duplicate asset_name in request", ObjectUtils.convertObjectToMap(request));
      });

    Map<UUID, AssetType> assetTypeMap = findAssetTypes(assetTypeSet);
    Map<UUID, Status> lifecycleStatusMap = findStatuses(lifecycleStatusSet, AssignmentStatusType.LIFECYCLE);
    Map<UUID, Status> stewardshipStatusMap = findStatuses(stewardshipStatusSet, AssignmentStatusType.STEWARDSHIP);

    Language language = languageService.getLanguage("ru");

    List<Asset> createdAssets = new ArrayList<>();
    assetsRequest.forEach(request -> {
      AssetType assetType = assetTypeMap.get(UUID.fromString(request.getAsset_type_id()));

      Status lifecycleStatus = null;
      if (StringUtils.isNotEmpty(request.getLifecycle_status())) {
        lifecycleStatus = lifecycleStatusMap.get(UUID.fromString(request.getLifecycle_status()));
      }

      Status stewardshipStatus = null;
      if (StringUtils.isNotEmpty(request.getStewardship_status())) {
        stewardshipStatus = stewardshipStatusMap.get(UUID.fromString(request.getStewardship_status()));
      }

      validateAssetNameByAssetTypeValidationMask(assetType.getAssetNameValidationMask(), request.getAsset_name(), assetType.getAssetNameValidationMaskExample());

      createdAssets.add(new Asset(
        request.getAsset_name(),
        assetType,
        request.getAsset_displayname(),
        language,
        lifecycleStatus,
        stewardshipStatus,
        user
      ));
    });

    try {
      return bulkAssetsService.createAssets(
        assetsRequest,
        assetTypeMap,
        lifecycleStatusMap,
        stewardshipStatusMap,
        user
      );
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      throw new AssetNameAlreadyExistsException(dataIntegrityViolationException);
    }
  }

  @Override
  @Transactional
  public AssetResponse updateAsset (
    UUID assetId,
    PostOrPatchAssetRequest assetRequest,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    AssetTypeNotFoundException,
    AssetTypeStatusAssignmentNotFoundException
  {
    LoggerWrapper.info("Starting updating asset " + assetId, AssetsServiceImpl.class.getName());

    Asset foundAsset = findAssetById(assetId);
    Status lifecycleStatus = null;
    Status stewardshipStatus = null;

    if (StringUtils.isNotEmpty(assetRequest.getAsset_type_id())) {
      AssetType assetType = assetTypesDAO.findAssetTypeById(UUID.fromString(assetRequest.getAsset_type_id()));
      foundAsset.setAssetType(assetType);
    }

    if (StringUtils.isNotEmpty(assetRequest.getAsset_name())) {
      foundAsset.setAssetName(assetRequest.getAsset_name());

      validateAssetNameByAssetTypeValidationMask(foundAsset.getAssetType().getAssetNameValidationMask(), assetRequest.getAsset_name(), foundAsset.getAssetType().getAssetNameValidationMaskExample());
    }

    if (StringUtils.isNotEmpty(assetRequest.getAsset_displayname())) {
      foundAsset.setAssetDisplayName(assetRequest.getAsset_displayname());
    }

    if (StringUtils.isNotEmpty(assetRequest.getLifecycle_status())) {
      lifecycleStatus = getStatus(assetRequest.getLifecycle_status(), AssignmentStatusType.LIFECYCLE);
    }

    if (lifecycleStatus != null) {
      foundAsset.setLifecycleStatus(lifecycleStatus);
    }

    if (StringUtils.isNotEmpty(assetRequest.getStewardship_status())) {
      stewardshipStatus = getStatus(assetRequest.getStewardship_status(), AssignmentStatusType.STEWARDSHIP);
    }

    if (stewardshipStatus != null) {
      foundAsset.setStewardshipStatus(stewardshipStatus);
    }

    foundAsset.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    foundAsset.setModifiedBy(user);

    assetRepository.save(foundAsset);
    createAssetHistory(foundAsset, MethodType.PATCH);

    AssetWithConnectedValues asset = assetRepository.getAssetByAssetIdWithJoinedTables(assetId);

    return new AssetResponse(
      asset.getAssetId(),
      asset.getAssetName(),
      asset.getAssetDisplayName(),
      asset.getAssetTypeId(),
      asset.getAssetTypeName(),
      asset.getLifecycleStatusId(),
      asset.getLifecycleStatusName(),
      asset.getStewardshipStatusId(),
      asset.getStewardshipStatusName(),
      asset.getLanguageName(),
      asset.getCreatedOn(),
      asset.getCreatedBy(),
      asset.getLastModifiedOn(),
      user.getUserId()
    );
  }

  @Override
  @Transactional
  public List<AssetResponse> updateBulkAsset (
    List<PatchAssetRequest> assetsRequest,
    User user
  ) throws
    AssetNotFoundException,
    StatusNotFoundException,
    AssetTypeNotFoundException,
    InvalidFieldLengthException,
    DuplicateValueInRequestException,
    AssetTypeStatusAssignmentNotFoundException,
    SomeRequiredFieldsAreEmptyException
  {
    LoggerWrapper.info("Starting update bulk asset", AssetsServiceImpl.class.getName());

    Set<UUID> assetsSet = new HashSet<>();
    Set<String> assetsNameSet = new HashSet<>();
    Map<UUID, UUID> assetAssetTypeMap = new HashMap<>();
    Map<AssignmentStatusType, List<AbstractMap.SimpleEntry<UUID, UUID>>> assignmentStatusAssetStatusMap = new HashMap<>();
    assignmentStatusAssetStatusMap.put(AssignmentStatusType.LIFECYCLE, new ArrayList<>());
    assignmentStatusAssetStatusMap.put(AssignmentStatusType.STEWARDSHIP, new ArrayList<>());

    assetsRequest.forEach(request -> {
      if (request.getAsset_id() == null) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      UUID assetId = request.getAsset_id();

      if (
        request.getAsset_type_id() == null &&
        StringUtils.isEmpty(request.getAsset_name()) &&
        request.getLifecycle_status() == null &&
        StringUtils.isEmpty(request.getAsset_displayname()) &&
        request.getStewardship_status() == null
      ) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      if (StringUtils.isNotEmpty(request.getAsset_name()) && request.getAsset_name().length() > 255) {
        throw new InvalidFieldLengthException(ObjectUtils.convertObjectToMap(request), "asset_name", 255);
      }

      if (StringUtils.isNotEmpty(request.getAsset_type_id())) {
        assetAssetTypeMap.put(assetId, UUID.fromString(request.getAsset_type_id()));
      }

      if (request.getLifecycle_status() != null) {
        putAssetStatusInMap(AssignmentStatusType.LIFECYCLE, new AbstractMap.SimpleEntry<>(assetId, request.getLifecycle_status()), assignmentStatusAssetStatusMap);
      }

      if (request.getStewardship_status()  != null) {
        putAssetStatusInMap(AssignmentStatusType.STEWARDSHIP, new AbstractMap.SimpleEntry<>(assetId, request.getStewardship_status()), assignmentStatusAssetStatusMap);
      }

      if (assetsSet.contains(assetId)) {
        throw new DuplicateValueInRequestException("Several entries for this asset_id", ObjectUtils.convertObjectToMap(request));
      }

      if (assetsNameSet.contains(request.getAsset_name())) {
        throw new DuplicateValueInRequestException("Asset name already exists.", ObjectUtils.convertObjectToMap(request));
      }

      if (StringUtils.isNotEmpty(request.getAsset_name())) {
        assetsNameSet.add(request.getAsset_name());
      }

      assetsSet.add(assetId);
    });

    Set<UUID> lifecycleStatusesSet = assignmentStatusAssetStatusMap.get(AssignmentStatusType.LIFECYCLE)
      .stream()
      .map(AbstractMap.SimpleEntry::getValue)
      .collect(Collectors.toSet());

    Set<UUID> stewardshipStatusesSet = assignmentStatusAssetStatusMap.get(AssignmentStatusType.STEWARDSHIP)
      .stream()
      .map(AbstractMap.SimpleEntry::getValue)
      .collect(Collectors.toSet());


    Map<UUID, Asset> assetsMap = findAssets(assetsSet);
    Map<UUID, AssetType> assetTypeMap = findAssetTypes(new HashSet<>(assetAssetTypeMap.values()));
    Map<UUID, Status> lifecycleStatusMap = findStatuses(lifecycleStatusesSet, AssignmentStatusType.LIFECYCLE);
    Map<UUID, Status> stewardshipStatusMap = findStatuses(stewardshipStatusesSet, AssignmentStatusType.STEWARDSHIP);

    try {
      return bulkAssetsService.updateAssets(
        assetsRequest,
        assetsMap,
        assetTypeMap,
        lifecycleStatusMap,
        stewardshipStatusMap,
        user
      );
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      throw new AssetNameAlreadyExistsException(dataIntegrityViolationException);
    }
  }

  @Override
  public AssetResponse getAssetById (UUID assetId) throws AssetNotFoundException {
    AssetWithConnectedValues asset = assetRepository.getAssetByAssetIdWithJoinedTables(assetId);

    if (asset == null) {
      throw new AssetNotFoundException(assetId);
    }

    if (asset.getIsDeleted()) {
      throw new AssetNotFoundException(assetId);
    }

    return getAssetResponse(asset);
  }

  @Override
  public GetAssetsResponse getAssetsByParams (
    GetAssetParams getAssetParams
  ) {
    LoggerWrapper.info("Starting getting asset by params", AssetsServiceImpl.class.getName());

    Integer pageSize = PageableUtils.getPageSize(getAssetParams.getPageSize());
    Integer pageNumber = PageableUtils.getPageNumber(getAssetParams.getPageNumber());

    String searchMode = getAssetParams.getAssetSearchMode() == null ? AssetSearchMode.ANY.toString() : getAssetParams.getAssetSearchMode().toString();
    Boolean isSearchAny = getAssetParams.getIsSearchAny() == null || getAssetParams.getIsSearchAny();
    Boolean rootFlag = getAssetParams.getRootFlag() != null && getAssetParams.getRootFlag();

    Page<AssetWithConnectedValues> assets = assetRepository.findAllByParamsWithJoinedTablesPageable(
      getAssetParams.getAssetName(),
      getAssetParams.getAssetDisplayName(),
      searchMode,
      isSearchAny,
      rootFlag,
      getAssetParams.getAssetTypeIds() == null ? 0 : getAssetParams.getAssetTypeIds().size(),
      getAssetParams.getAssetTypeIds(),
      getAssetParams.getLifecycleStatuses() == null ? 0 : getAssetParams.getLifecycleStatuses().size(),
      getAssetParams.getLifecycleStatuses(),
      getAssetParams.getStewardshipStatuses() == null ? 0 : getAssetParams.getStewardshipStatuses().size(),
      getAssetParams.getStewardshipStatuses(),
      PageRequest.of(pageNumber, pageSize, SortUtils.getSort(getAssetParams.getSortOrder(), getAssetParams.getSortField() != null ? getAssetParams.getSortField().getValue() : SortField.ASSET_NAME.getValue(), SortField.ASSET_NAME.getValue()))
    );

    List<AssetResponse> assetsList = assets.stream().map(this::getAssetResponse).toList();

    return new GetAssetsResponse(
      assets.getTotalElements(),
      pageSize,
      pageNumber,
      assetsList
    );
  }

  @Override
  public GetAssetChangeHistory getAssetChangeHistory (UUID assetId, GetChangeHistoryParams params) throws AssetNotFoundException {
    boolean isAssetExists = assetRepository.existsById(assetId);
    if (!isAssetExists) {
      throw new AssetNotFoundException(assetId);
    }

    Integer pageSize = PageableUtils.getPageSize(params.getPageSize(), 25);
    Integer pageNumber = PageableUtils.getPageNumber(params.getPageNumber());

    AssetChangeHistoryDTO changeHistoryDTO = new AssetChangeHistoryDTO(params.getActionTypes(), params.getEntityTypes());
    AbstractMap.SimpleEntry<String, String> queryStrings = changeHistoryDTO.prepareRequestString();

    String countQueryString = queryStrings.getKey();
    String selectQueryString = queryStrings.getValue();

    if (countQueryString == null) {
      return new GetAssetChangeHistory(
        0,
        pageSize,
        pageNumber,
        new ArrayList<>()
      );
    }

    Long total = countTotalChangeHistory(countQueryString, assetId, params);

    if (total == 0) {
      return new GetAssetChangeHistory(
        0,
        pageSize,
        pageNumber,
        new ArrayList<>()
      );
    }

    PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by("loggedOn").descending());
    List<AssetChangeHistoryResponse> response = selectChangeHistory(selectQueryString, assetId, params, pageRequest);
    List<GetAssetChangeHistory.ChangeHistory> changeHistories = response.stream().map(item -> new GetAssetChangeHistory.ChangeHistory(
      item.getLogged_on(),
      item.getLast_name(),
      item.getFirst_name(),
      item.getUsername(),
      item.getUser_id(),
      item.getEntity_type_name_ru(),
      item.getEntity_type_name(),
      item.getAction_type_name_ru(),
      item.getAction_type_name(),
      item.getObject_id(),
      item.getObject_type_id(),
      item.getObject_type_name(),
      item.getValue()
    )).toList();

    return new GetAssetChangeHistory(
      total,
      pageSize,
      pageNumber,
      changeHistories
    );
  }

  @Override
  @Transactional
  public void deleteAssetById (UUID assetId, User user) throws AssetNotFoundException {
    LoggerWrapper.info("Starting deleting asset " + assetId, AssetsServiceImpl.class.getName());

    Asset foundAsset = findAssetById(assetId);

    List<Relation> relations = relationsDAO.findAllByAssetIds(List.of(assetId));
    List<UUID> relationIds = relations.stream().map(Relation::getRelationId).toList();
    relationsDAO.deleteRelationsByIds(relationIds, user);

    responsibilitiesDAO.deleteAllByParams(assetId, null, null, null, null, null, user);
    assetLinkUsageDAO.deleteAssetLinkByAssetIds(List.of(assetId), user);
    subscriptionsDAO.deleteAllByAssetIds(List.of(assetId), user);
    attributesDAO.deleteAllByAssetIds(List.of(assetId), user);

    foundAsset.setIsDeleted(true);
    foundAsset.setDeletedBy(user);
    foundAsset.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    assetRepository.save(foundAsset);
    createAssetHistory(foundAsset, MethodType.DELETE);

    roleActionCachingService.evictByValueInKey(assetId.toString());
  }

  @Override
  @Transactional
  public void deleteAssetsBulk (List<UUID> assetsRequest, User user) throws AssetNotFoundException, DuplicateValueInRequestException {
    LoggerWrapper.info("Starting deleting assets bulk", AssetsServiceImpl.class.getName());

    UUID firstDuplicate = CollectionUtils.findFirstDuplicate(assetsRequest);
    if (firstDuplicate != null) {
      throw new DuplicateValueInRequestException("Duplicate asset_id in request", Map.of("asset_id", firstDuplicate));
    }

    List<UUID> assetUUIDs = assetRepository.findIdsByAssetIds(assetsRequest);

    if (assetsRequest.size() != assetUUIDs.size()) {
      UUID firstNotFoundValue = CollectionUtils.findFirstNotFoundValue(
        new HashSet<>(assetsRequest),
        new HashSet<>(assetUUIDs)
      );

      throw new AssetNotFoundException(Map.of("asset_id", firstNotFoundValue));
    }

    Set<UUID> relationIds = relationsDAO.findAllRelationIdByAssetIds(assetsRequest);
    relationsDAO.deleteRelationsByIds(relationIds.stream().toList(), user);
    assetRepository.deleteAllByAssetIds(assetsRequest, user.getUserId());
    responsibilitiesDAO.deleteAllByAssetIds(assetsRequest, user);
    assetLinkUsageDAO.deleteAssetLinkByAssetIds(assetsRequest, user);
    subscriptionsDAO.deleteAllByAssetIds(assetsRequest, user);
    attributesDAO.deleteAllByAssetIds(assetsRequest, user);

    assetsRequest.forEach(assetId -> {
      assetHistoryRepository.createAssetHistoryFromAsset(UUID.randomUUID(), assetId);
      assetHistoryRepository.updateLastAssetHistoryByDeletedAssetId(assetId, HistoryDateUtils.getValidToDefaultTime());
    });

    assetsRequest.forEach(assetId -> roleActionCachingService.evictByValueInKey(assetId.toString()));
  }

  @Override
  public GetAssetHeaderResponse getAssetHeader (UUID assetId) throws AssetNotFoundException {
    LoggerWrapper.info("Starting getting asset header for asset " + assetId, AssetsServiceImpl.class.getName());

    UUID attributeTypeId = UUID.fromString("00000000-0000-0000-0000-000000003114");
    Optional<AssetWithDescription> assetWithDetails = assetRepository.findAssetWithDetails(assetId, attributeTypeId);

    if (assetWithDetails.isEmpty()) {
      throw new AssetNotFoundException();
    }

    AssetWithDescription asset = assetWithDetails.get();

    List<AssetTypeCardHeaderAssignmentResponsible> assignmentResponsible = assetTypeCardHeaderAssignmentDAO.findAllAssetCardHeaderResponsibleByAssetId(assetId);
    List<GetAssetHeaderBusinessOwnerResponse> businessOwners = assignmentResponsible.stream()
      .filter(responsible -> !Stream.of(
        responsible.getResponsibilityId(),
        responsible.getResponsibleId()
      ).allMatch(Objects::isNull))
      .map(item -> new GetAssetHeaderBusinessOwnerResponse(
        item.getResponsibilityId(),
        item.getResponsibleType(),
        item.getResponsibleId(),
        item.getResponsibleName(),
        item.getResponsibleFullName()
      )).toList();

    return new GetAssetHeaderResponse(
      asset.getAssetId(),
      asset.getAssetName(),
      asset.getAssetDisplayName(),
      asset.getAssetTypeId(),
      asset.getAssetTypeName(),
      asset.getLifecycleStatusId(),
      asset.getLifecycleStatusName(),
      asset.getStewardshipStatusId(),
      asset.getStewardshipStatusName(),
      asset.getDescription(),
      businessOwners,
      asset.isHasCustomViews(),
      asset.getSourceLanguage(),
      asset.getCreatedOn(),
      asset.getCreatedBy(),
      asset.getLastModifiedOn(),
      asset.getLastModifiedBy()
    );
  }

  @Override
  public GetAssetsChildrenResponse getAssetsChildren (
    UUID assetId,
    String assetDisplayname,
    List<UUID> assetTypeIds,
    List<UUID> lifecycleStatusIds,
    List<UUID> stewardshipStatusIds,
    ChildrenSortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  ) {
    LoggerWrapper.info("Starting getting asset children for asset " + assetId, AssetsServiceImpl.class.getName());

    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AssetChildren> assetsChildren = assetRepository.getAssetsChildren(
      assetId,
      assetDisplayname,
      assetTypeIds != null ? assetTypeIds.size() : 0,
      assetTypeIds,
      lifecycleStatusIds != null ? lifecycleStatusIds.size() : 0,
      lifecycleStatusIds,
      stewardshipStatusIds != null ? stewardshipStatusIds.size() : 0,
      stewardshipStatusIds,
      PageRequest.of(pageNumber, pageSize, SortUtils.getSort(sortOrder, sortField != null ? sortField.getValue() : null, ChildrenSortField.DISPLAYNAME.getValue()))
    );

    List<GetAssetChildrenResponse> list = assetsChildren.stream()
      .map(assetChildren -> new GetAssetChildrenResponse(
          assetChildren.getAssetId(),
          assetChildren.getName(),
          assetChildren.getDisplayname(),
          assetChildren.getAssetTypeId(),
          assetChildren.getAssetTypeName(),
          assetChildren.getDescription(),
          assetChildren.getLifecycleStatusId(),
          assetChildren.getLifecycleStatusName(),
          assetChildren.getStewardshipStatusId(),
          assetChildren.getStewardshipStatusName(),
          assetChildren.getChildrenCount()
        )
      ).toList();

    return new GetAssetsChildrenResponse(
      assetsChildren.getTotalElements(),
      pageSize,
      pageNumber,
      list
    );
  }

  @Override
  public GetAssetPathElementsResponse getAssetPath (UUID assetId) {
    LoggerWrapper.info("Starting getting asset path for asset " + assetId, AssetsServiceImpl.class.getName());

    String assetHierarchyPath = assetRepository.getAssetHierarchyPath(assetId);
    List<AssetHierarchyPathElement> assetHierarchyPathElements = assetRepository.getAssetHierarchyPathElements(assetId);

    GetAssetPathElementResponse pathElementResponse = new GetAssetPathElementResponse(
      assetHierarchyPathElements.size(),
      assetHierarchyPathElements.stream()
        .map(element -> new GetPathElementResponse(
          element.getAssetDisplayName(),
          element.getAssetName(),
          element.getAssetId()
        )).toList()
    );

    return new GetAssetPathElementsResponse(
      assetHierarchyPath,
      pathElementResponse
    );
  }

  @Override
  public GetAssetRelationTypes getAssetRelationTypes (UUID assetId) {
    LoggerWrapper.info("Starting getting asset relation types for asset " + assetId, AssetsServiceImpl.class.getName());

    List<AssetRelationType> assetRelationTypes = assetRepository.getAssetRelationType(assetId);
    List<AssetRelationTypeComponent> assetRelationTypeComponents = assetRepository.getAssetRelationTypeComponents(assetId);

    Map<UUID, List<AssetRelationTypeComponent>> relationTypeComponentsMap = assetRelationTypeComponents.stream()
      .collect(Collectors.groupingBy(AssetRelationTypeComponent::getRelationTypeId));


    List<GetAssetRelationType> relationTypeList = assetRelationTypes.stream()
      .sorted(Comparator.comparing(AssetRelationType::getRelationTypeName))
      .map(relationType -> {
        List<AssetRelationTypeComponent> relationTypeComponents = relationTypeComponentsMap.get(relationType.getRelationTypeId());

        return new GetAssetRelationType(
          relationType,
          relationTypeComponents
        );
      }).toList();

    return new GetAssetRelationTypes(
      assetRelationTypes.size(),
      relationTypeList
    );
  }

  @Override
  public GetAssetAttributeLinksUsageResponse getAssetAttributeLinkUsage (
    UUID assetId,
    List<UUID> assetTypeIds,
    List<UUID> attributeTypeIds,
    List<UUID> lifecycleStatusIds,
    List<UUID> stewardshipStatusIds,
    Integer pageNumber,
    Integer pageSize
  ) {
    LoggerWrapper.info("Starting getting asset attribute link usage for asset " + assetId, AssetsServiceImpl.class.getName());

    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AssetAttributeLinkUsage> assetAttributeLinkUsage = assetRepository.findAllAssetAttributeLinkUsageByAssetIdPageable(
      assetId,
      assetTypeIds != null ? assetTypeIds.size() : 0,
      assetTypeIds,
      attributeTypeIds != null ? attributeTypeIds.size() : 0,
      attributeTypeIds,
      lifecycleStatusIds != null ? lifecycleStatusIds.size() : 0,
      lifecycleStatusIds,
      stewardshipStatusIds != null ? stewardshipStatusIds.size() : 0,
      stewardshipStatusIds,
      PageRequest.of(pageNumber, pageSize, Sort.by("assetLinkUsageId").ascending())
    );

    List<GetAssetAttributeLinkResponse> linkResponses = assetAttributeLinkUsage.stream().map(GetAssetAttributeLinkResponse::new).toList();

    return new GetAssetAttributeLinksUsageResponse(
      assetAttributeLinkUsage.getTotalElements(),
      pageSize,
      pageNumber,
      linkResponses
    );
  }

  private void putAssetStatusInMap (
    AssignmentStatusType assignmentStatusType,
    AbstractMap.SimpleEntry<UUID, UUID> entry,
    Map<AssignmentStatusType, List<AbstractMap.SimpleEntry<UUID, UUID>>> assignmentStatusAssetStatusMap
  ) {
    assignmentStatusAssetStatusMap
      .compute(assignmentStatusType, (_key, value) -> {
        if (value == null) {
          value = new ArrayList<>();
        }

        value.add(entry);

        return value;
      });
  }

  private AssetResponse getAssetResponse (AssetWithConnectedValues asset) {
    return new AssetResponse(
      asset.getAssetId(),
      asset.getAssetName(),
      asset.getAssetDisplayName(),
      asset.getAssetTypeId(),
      asset.getAssetTypeName(),
      asset.getLifecycleStatusId(),
      asset.getLifecycleStatusName(),
      asset.getStewardshipStatusId(),
      asset.getStewardshipStatusName(),
      asset.getLanguageName(),
      asset.getCreatedOn(),
      asset.getCreatedBy(),
      asset.getLastModifiedOn(),
      asset.getLastModifiedBy()
    );
  }

  private Status getStatus (String status, AssignmentStatusType assignmentStatusType) throws AssetTypeStatusAssignmentNotFoundException {
    if (StringUtils.isEmpty(status)) return null;

    UUID uuid = UUID.fromString(status);

    checkAssetTypeStatusAssignments(uuid, assignmentStatusType);

    return statusesDAO.findStatusById(uuid);
  }

  private void checkAssetTypeStatusAssignments (
    UUID statusId,
    AssignmentStatusType assignmentStatusType
  ) throws AssetTypeStatusAssignmentNotFoundException {
    Boolean hasAssets = assetTypeStatusesAssignmentsDAO.isAssignmentsExistsByStatusIdAndAssignmentStatusType(
      statusId,
      assignmentStatusType
    );

    if (!hasAssets) {
      throw new AssetTypeStatusAssignmentNotFoundException(statusId);
    }
  }

  private Map<UUID, Asset> findAssets (Set<UUID> assets) throws AssetTypeNotFoundException {
    List<Asset> assetsList = assetRepository.findAllByAssetIds(assets.stream().toList());

    Map<UUID, Asset> assetsMap = assetsList.stream().collect(Collectors.toMap(Asset::getAssetId, value -> value));

    if (assetsList.size() != assets.size()) {
      UUID absentAssetId = CollectionUtils.findFirstNotFoundValue(assets, assetsMap.keySet());

      PatchAssetRequest request = new PatchAssetRequest();
      request.setAsset_id(absentAssetId);

      throw new AssetNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return assetsMap;
  }

  private Map<UUID, AssetType> findAssetTypes (Set<UUID> assetTypeSet) throws AssetTypeNotFoundException {
    List<AssetType> assetTypeList = assetTypesDAO.findAssetTypeByIds(assetTypeSet.stream().toList());

    Map<UUID, AssetType> assetTypeMap = assetTypeList.stream().collect(Collectors.toMap(AssetType::getAssetTypeId, value -> value));

    if (assetTypeList.size() != assetTypeSet.size()) {
      UUID absentAssetTypeId = CollectionUtils.findFirstNotFoundValue(assetTypeSet, assetTypeMap.keySet());

      PostOrPatchAssetRequest request = new PostOrPatchAssetRequest();
      request.setAsset_type_id(absentAssetTypeId.toString());

      throw new AssetTypeNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return assetTypeMap;
  }

  private Map<UUID, Status> findStatuses (Set<UUID> statusesSet, AssignmentStatusType assignmentStatusType) throws StatusNotFoundException {
    List<UUID> statusesList = statusesSet.stream().toList();

    Set<UUID> lifecycleStatusesAssignment = assetTypeStatusesAssignmentsDAO.getAssetTypeStatusAssignmentsByStatusIdAndAssignmentStatusType(statusesList, assignmentStatusType);

    if (lifecycleStatusesAssignment.size() != statusesSet.size()) {
      UUID absentStatusId = CollectionUtils.findFirstNotFoundValue(statusesSet, lifecycleStatusesAssignment);

      PostOrPatchAssetRequest request = new PostOrPatchAssetRequest();
      if (assignmentStatusType.equals(AssignmentStatusType.LIFECYCLE)) {
        request.setLifecycle_status(absentStatusId.toString());
      }

      if (assignmentStatusType.equals(AssignmentStatusType.STEWARDSHIP)) {
        request.setStewardship_status(absentStatusId.toString());
      }

      throw new AssetTypeStatusAssignmentNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    List<Status> statuses = statusesDAO.findAllByStatusIds(statusesList);

    return statuses.stream().collect(Collectors.toMap(Status::getStatusId, value -> value));
  }

  private Long countTotalChangeHistory (
    String countQueryString,
    UUID assetId,
    GetChangeHistoryParams params
  ) {
    Query countQuery = entityManager.createNativeQuery(countQueryString);

    prepareAssetChangeHistoryCountQueryParams(
      countQuery,
      assetId,
      params.getMinDate(),
      params.getMaxDate(),
      params.getUserIds(),
      params.getActionTypes(),
      params.getEntityTypes()
    );

    BigDecimal totalCount = (BigDecimal) countQuery.getSingleResult();

    return totalCount == null ? 0 : totalCount.longValue();
  }

  private List<AssetChangeHistoryResponse> selectChangeHistory (
    String selectQueryString,
    UUID assetId,
    GetChangeHistoryParams params,
    PageRequest pageRequest
  ) {
    String sortString = generateSortString(pageRequest);
    Query selectQuery = entityManager.createNativeQuery(selectQueryString + sortString);
    selectQuery.setFirstResult(pageRequest.getPageNumber() * pageRequest.getPageSize());
    selectQuery.setMaxResults(pageRequest.getPageSize());

    prepareAssetChangeHistoryQueryParams(
      selectQuery,
      assetId,
      new java.sql.Timestamp(params.getMinDate().getTime()),
      new java.sql.Timestamp(params.getMaxDate().getTime()),
      params.getUserIds()
    );

    return selectQuery
      .unwrap(org.hibernate.query.NativeQuery.class)
      .setResultTransformer(Transformers.aliasToBean(AssetChangeHistoryResponse.class))
      .getResultList();
  }

  private void prepareAssetChangeHistoryCountQueryParams (
    Query query,
    UUID assetId,
    Date minTime,
    Date maxTime,
    List<UUID> userIds,
    List<AssetHistoryActionType> actionTypes,
    List<AssetHistoryEntityType> entityTypes
  ) {
    prepareAssetChangeHistoryQueryParams(
      query,
      assetId,
      new java.sql.Timestamp(minTime.getTime()),
      new java.sql.Timestamp(maxTime.getTime()),
      userIds
    );

    query.setParameter("actionTypesCount", actionTypes == null ? 0 : actionTypes.size());
    query.setParameter("actionTypes", actionTypes == null ? null : actionTypes.stream().map(AssetHistoryActionType::toString).toList());
    query.setParameter("entityTypesCount", entityTypes == null ? 0 : entityTypes.size());
    query.setParameter("entityTypes", entityTypes == null ? null : entityTypes.stream().map(AssetHistoryEntityType::toString).toList());
  }

  private void prepareAssetChangeHistoryQueryParams (
    Query query,
    UUID assetId,
    java.sql.Timestamp minTime,
    java.sql.Timestamp maxTime,
    List<UUID> userIds
  ) {
    query.setParameter("assetId", assetId);
    query.setParameter("minTime", minTime);
    query.setParameter("maxTime", maxTime);
    query.setParameter("userIdsCount", userIds == null ? 0 : userIds.size());
    query.setParameter("userIds", userIds);
  }

  private String generateSortString (PageRequest pageRequest) {
    Sort sort = pageRequest.getSort();

    if (!sort.isSorted()) return "";

    StringBuilder orderBy = new StringBuilder(" ORDER BY ");
    boolean isFirst = true;

    for (Sort.Order order : sort) {
      String property = order.getProperty();
      String direction = order.getDirection().toString();

      if (!isFirst) {
        orderBy.append(", ");
      }
      orderBy.append(property).append(" ").append(direction);
      isFirst = false;
    }

    return orderBy.toString();
  }
}
