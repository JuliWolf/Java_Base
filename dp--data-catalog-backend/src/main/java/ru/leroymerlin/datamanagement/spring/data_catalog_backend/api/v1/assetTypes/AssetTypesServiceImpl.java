package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.models.AssetTypeChild;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.models.AssetTypeWithRootFlag;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypeInheritance.AssetTypeInheritanceDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetNameValidationMaskDoesNotMatchExampleException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetNameValidationMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeHasChildAssetTypesException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypeChildrenResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PatchAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.AssetTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.AssetTypeRelationTypeComponentAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.AssetTypeStatusesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.CustomViewsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.RoleActionsDAO;

/**
 * @author JuliWolf
 */
@Service
public class AssetTypesServiceImpl extends AssetTypesDAO implements AssetTypesService {
  private final RoleActionCachingService roleActionCachingService;

  private final LanguageService languageService;

  private final RoleActionsDAO roleActionsDAO;

  private final AssetTypeInheritanceDAO assetTypeInheritanceDAO;

  private final AssetTypeStatusesAssignmentsDAO assetTypeStatusesAssignmentsDAO;

  private final AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO;

  private final AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO;

  private final RelationTypeComponentAssetTypesAssignmentsDAO relationTypeComponentAssetTypesAssignmentsDAO;

  private final CustomViewsDAO customViewsDAO;

  private final AssetTypeRelationTypeComponentAssignmentsDAO assetTypeRelationTypeComponentAssignmentsDAO;

  @Autowired
  public AssetTypesServiceImpl (
    CustomViewsDAO customViewsDAO,
    RoleActionsDAO roleActionsDAO,
    LanguageService languageService,
    AssetTypeRepository assetTypeRepository,
    AssetTypeInheritanceDAO assetTypeInheritanceDAO,
    RoleActionCachingService roleActionCachingService,
    AssetTypeStatusesAssignmentsDAO assetTypeStatusesAssignmentsDAO,
    AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO,
    AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO,
    AssetTypeRelationTypeComponentAssignmentsDAO assetTypeRelationTypeComponentAssignmentsDAO,
    RelationTypeComponentAssetTypesAssignmentsDAO relationTypeComponentAssetTypesAssignmentsDAO
  ) {
    super(assetTypeRepository);

    this.customViewsDAO = customViewsDAO;
    this.roleActionsDAO = roleActionsDAO;
    this.languageService = languageService;
    this.assetTypeInheritanceDAO = assetTypeInheritanceDAO;
    this.roleActionCachingService = roleActionCachingService;
    this.assetTypeStatusesAssignmentsDAO = assetTypeStatusesAssignmentsDAO;
    this.assetTypeCardHeaderAssignmentDAO = assetTypeCardHeaderAssignmentDAO;
    this.assetTypeAttributeTypesAssignmentsDAO = assetTypeAttributeTypesAssignmentsDAO;
    this.assetTypeRelationTypeComponentAssignmentsDAO = assetTypeRelationTypeComponentAssignmentsDAO;
    this.relationTypeComponentAssetTypesAssignmentsDAO = relationTypeComponentAssetTypesAssignmentsDAO;
  }

  @Override
  @Transactional
  public PostAssetTypeResponse createAssetType(
    PostAssetTypeRequest assetTypeRequest,
    User user
  ) throws
    IllegalArgumentException,
    AssetTypeNotFoundException,
    AssetNameValidationMaskValidationException,
    AssetNameValidationMaskDoesNotMatchExampleException
  {
    Language ru = languageService.getLanguage("ru");

    validateAssetNameValidationMask(
      assetTypeRequest.getAsset_name_validation_mask(),
      assetTypeRequest.getAsset_name_validation_mask_example()
    );

    AssetType assetType = assetTypeRepository.save(new AssetType(
        assetTypeRequest.getAsset_type_name(),
        assetTypeRequest.getAsset_type_description(),
        assetTypeRequest.getAsset_type_acronym(),
        assetTypeRequest.getAsset_type_color(),
        assetTypeRequest.getAsset_name_validation_mask(),
        assetTypeRequest.getAsset_name_validation_mask_example(),
        ru,
        user
    ));

    // Clear asset type if someone tried to get asset type with this id
    roleActionCachingService.evictByValueInKey(assetType.getAssetTypeId().toString());

    if (StringUtils.isNotEmpty(assetTypeRequest.getParent_asset_type_id())) {
      AssetType parentAssetType = findAssetTypeById(UUID.fromString(assetTypeRequest.getParent_asset_type_id()));
      assetTypeInheritanceDAO.saveAssetTypeInheritance(new AssetTypeInheritance(parentAssetType, assetType, user));

      createAssetTypeStatusAssignmentsFromParentAssetType(parentAssetType, assetType, user);
      createAssetTypeAttributeAssignmentsFromParentAssetType(parentAssetType, assetType, user);
      createRelationTypeComponentAssetTypeAssignmentsFromParentAssetType(parentAssetType, assetType, user);
    }

    return new PostAssetTypeResponse(
        assetType.getAssetTypeId(),
        assetType.getAssetTypeName(),
        assetType.getAssetTypeDescription(),
        assetType.getLanguageName(),
        assetType.getAssetTypeAcronym(),
        assetType.getAssetTypeColor(),
        assetType.getAssetNameValidationMask(),
        assetType.getAssetNameValidationMaskExample(),
        assetType.getCreatedOn(),
        assetType.getCreatedByUUID(),
        null,
        null
    );
  }

  @Override
  public PostAssetTypeResponse updateAssetType (UUID assetTypeId, PatchAssetTypeRequest request, User user) {
    AssetType foundAssetType = findAssetTypeById(assetTypeId);

    OptionalUtils.doActionIfPresent(request.getAsset_type_name(), name -> foundAssetType.setAssetTypeName(name.orElse(foundAssetType.getAssetTypeName())));
    OptionalUtils.doActionIfPresent(request.getAsset_type_description(), description -> foundAssetType.setAssetTypeDescription(description.orElse(null)));
    OptionalUtils.doActionIfPresent(request.getAsset_name_validation_mask(), validationMask -> {
      validateAssetNameValidationMask(
        validationMask.orElse(null),
        request.getAsset_name_validation_mask_example() == null
          ? foundAssetType.getAssetNameValidationMaskExample()
          : request.getAsset_name_validation_mask_example().orElse(null)
      );

      foundAssetType.setAssetNameValidationMask(validationMask.orElse(null));
    });

    OptionalUtils.doActionIfPresent(request.getAsset_name_validation_mask_example(), validationMaskExample -> {
      validateAssetNameValidationMask(
        request.getAsset_name_validation_mask() == null
          ? foundAssetType.getAssetNameValidationMask()
          : request.getAsset_name_validation_mask().orElse(null),
        validationMaskExample.orElse(null)
      );

      foundAssetType.setAssetNameValidationMaskExample(validationMaskExample.orElse(null));
    });

    if (StringUtils.isNotEmpty(request.getAsset_type_acronym())) {
      foundAssetType.setAssetTypeAcronym(request.getAsset_type_acronym());
    }

    if (StringUtils.isNotEmpty(request.getAsset_type_color())) {
      foundAssetType.setAssetTypeColor(request.getAsset_type_color());
    }

    foundAssetType.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    foundAssetType.setModifiedBy(user);

    AssetType assetType = assetTypeRepository.save(foundAssetType);

    return new PostAssetTypeResponse(
      assetType.getAssetTypeId(),
      assetType.getAssetTypeName(),
      assetType.getAssetTypeDescription(),
      assetType.getLanguageName(),
      assetType.getAssetTypeAcronym(),
      assetType.getAssetTypeColor(),
      assetType.getAssetNameValidationMask(),
      assetType.getAssetNameValidationMaskExample(),
      assetType.getCreatedOn(),
      assetType.getCreatedByUUID(),
      assetType.getLastModifiedOn(),
      user.getUserId()
    );
  }

  @Override
  public GetAssetTypesResponse geAssetTypesByParams (
    Boolean rootFlag,
    String assetTypeName,
    String assetTypeDescription,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AssetTypeWithRootFlag> assetTypes = assetTypeRepository.findAllByAssetTypeNameAndDescriptionPageable(
      rootFlag,
      assetTypeName,
      assetTypeDescription,
      PageRequest.of(pageNumber, pageSize, Sort.by("asset_type_name").ascending())
    );

    List<GetAssetTypeResponse> assetTypeCollection = assetTypes.stream()
      .map(assetType -> new GetAssetTypeResponse(
      assetType.getAssetTypeId(),
      assetType.getAssetTypeName(),
      assetType.getAssetTypeDescription(),
      assetType.getLanguageName(),
      assetType.getAssetTypeAcronym(),
      assetType.getAssetTypeColor(),
      assetType.getAssetNameValidationMask(),
      assetType.getAssetNameValidationMaskExample(),
      assetType.getRootFlag(),
      assetType.getCreatedOn(),
      assetType.getCreatedBy(),
      assetType.getLastModifiedOn(),
      assetType.getLastModifiedBy()
    )).toList();

    return new GetAssetTypesResponse(
      assetTypes.getTotalElements(),
      pageSize,
      pageNumber,
      assetTypeCollection
    );
  }

  @Override
  public GetAssetTypeResponse getAssetTypeById (
    UUID assetTypeId
  ) throws AssetTypeNotFoundException {
    AssetType assetType = findAssetTypeById(assetTypeId);

    UUID lastModifiedBy = assetType.getModifiedBy() != null ? assetType.getModifiedBy().getUserId() : null;

    return new GetAssetTypeResponse(
      assetType.getAssetTypeId(),
      assetType.getAssetTypeName(),
      assetType.getAssetTypeDescription(),
      assetType.getLanguageName(),
      assetType.getAssetTypeAcronym(),
      assetType.getAssetTypeColor(),
      assetType.getAssetNameValidationMask(),
      assetType.getAssetNameValidationMaskExample(),
      null,
      assetType.getCreatedOn(),
      assetType.getCreatedByUUID(),
      assetType.getLastModifiedOn(),
      lastModifiedBy
    );
  }

  @Override
  @Transactional
  public void deleteAssetTypeById (UUID assetTypeId, User user) throws AssetTypeNotFoundException, AssetTypeHasChildAssetTypesException {
    AssetType foundAssetType = findAssetTypeById(assetTypeId);

    Boolean hasChildAssets = assetTypeInheritanceDAO.isAssetTypeInheritanceExistsByParentAndChildAssetType(foundAssetType.getAssetTypeId(), null);
    if (hasChildAssets) {
      throw new AssetTypeHasChildAssetTypesException();
    }

    customViewsDAO.deleteByParams(assetTypeId, null, user);
    roleActionsDAO.deleteAllByParams(null, assetTypeId, null, null, user);
    assetTypeInheritanceDAO.deleteAllByChildAssetTypeId(foundAssetType.getAssetTypeId(), user);
    assetTypeStatusesAssignmentsDAO.deleteAllByAssetTypeId(foundAssetType.getAssetTypeId(), user);
    assetTypeCardHeaderAssignmentDAO.deleteAssetTypeCardHeaderAssignmentByParams(null, assetTypeId, null, user);
    assetTypeAttributeTypesAssignmentsDAO.deleteAllByParams(foundAssetType.getAssetTypeId(), null, user);
    relationTypeComponentAssetTypesAssignmentsDAO.deleteAllByAssetTypeIds(foundAssetType.getAssetTypeId(), user);

    foundAssetType.setIsDeleted(true);
    foundAssetType.setDeletedBy(user);
    foundAssetType.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    assetTypeRepository.save(foundAssetType);

    roleActionCachingService.evictByValueInKey(assetTypeId.toString());
  }

  @Override
  public GetAssetTypeChildrenResponse getAssetTypeChildren (
    UUID assetTypeId,
    Integer pageNumber,
    Integer pageSize
  ) throws AssetTypeNotFoundException {
    boolean isExists = assetTypeRepository.existsByAssetTypeIdAndIsDeletedFalse(assetTypeId);

    if (!isExists) {
      throw new AssetTypeNotFoundException();
    }

    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AssetTypeChild> response = assetTypeRepository.findAllAssetTypeChildrenPageable(
      assetTypeId,
      PageRequest.of(pageNumber, pageSize, Sort.by("at.asset_type_name").ascending())
    );

    List<GetAssetTypeChildrenResponse.GetAssetTypeChildResponse> assetTypeChildren = response.stream()
      .map(child -> new GetAssetTypeChildrenResponse.GetAssetTypeChildResponse(
        child.getAssetTypeId(),
        child.getAssetTypeName(),
        child.getAssetTypeDescription(),
        child.getChildrenAssetTypeCount()
      )).toList();

    return new GetAssetTypeChildrenResponse(
      response.getTotalElements(),
      pageSize,
      pageNumber,
      assetTypeChildren
    );
  }

  private void createAssetTypeAttributeAssignmentsFromParentAssetType (AssetType parentAssetType, AssetType childAssetType, User user) {
    List<AssetTypeAttributeTypeAssignment> parentAssignments = assetTypeAttributeTypesAssignmentsDAO.findAllByAssetTypeId(parentAssetType.getAssetTypeId());

    parentAssignments.forEach(assignment -> {
      AssetTypeAttributeTypeAssignment assetTypeAttributeTypeAssignment = new AssetTypeAttributeTypeAssignment(childAssetType, assignment.getAttributeType(), user);
      assetTypeAttributeTypeAssignment.setIsInherited(true);
      assetTypeAttributeTypeAssignment.setParentAssetType(parentAssetType);
      assetTypeAttributeTypesAssignmentsDAO.saveAssetTypeAttributeTypesAssignment(assetTypeAttributeTypeAssignment);
    });
  }

  private void createAssetTypeStatusAssignmentsFromParentAssetType (AssetType parentAssetType, AssetType childAssetType, User user) {
    List<AssetTypeStatusAssignment> statusesAssignments = assetTypeStatusesAssignmentsDAO.findAllByAssetTypeIdAndDeletedIsFalse(parentAssetType.getAssetTypeId());

    statusesAssignments.forEach(assignment -> {
      AssetTypeStatusAssignment assetTypeStatusAssignment = new AssetTypeStatusAssignment(childAssetType, assignment.getAssignmentStatusType(), assignment.getStatus(), user);
      assetTypeStatusAssignment.setIsInherited(true);
      assetTypeStatusAssignment.setParentAssetType(parentAssetType);
      assetTypeStatusesAssignmentsDAO.saveAssetTypeAttributeTypesAssignment(assetTypeStatusAssignment);
    });
  }

  private void createRelationTypeComponentAssetTypeAssignmentsFromParentAssetType (AssetType parentAssetType, AssetType childAssetType, User user) {
    List<RelationTypeComponentAssetTypeAssignment> assetTypeRelationTypeComponentAssignments = assetTypeRelationTypeComponentAssignmentsDAO.findAllByAssetTypeId(parentAssetType.getAssetTypeId());

    assetTypeRelationTypeComponentAssignments.forEach(assignment -> {
      RelationTypeComponentAssetTypeAssignment relationTypeComponentAssetTypeAssignment = new RelationTypeComponentAssetTypeAssignment(assignment.getRelationTypeComponent(), childAssetType, true, parentAssetType, user);
      assetTypeRelationTypeComponentAssignmentsDAO.saveRelationTypeComponentAssetTypeAssignment(relationTypeComponentAssetTypeAssignment);
    });
  }

  private void validateAssetNameValidationMask (String assetNameValidationMask, String assetNameValidationMaskExample) {
    boolean isMaskEmpty = StringUtils.isEmpty(assetNameValidationMask);
    boolean isMaskExampleEmpty = StringUtils.isEmpty(assetNameValidationMaskExample);

    if (isMaskEmpty && isMaskExampleEmpty) return;

    if (
      (isMaskEmpty && !isMaskExampleEmpty) ||
      (!isMaskEmpty && isMaskExampleEmpty)
    ) {
      throw new AssetNameValidationMaskValidationException();
    }

    boolean isValid = Pattern.matches(assetNameValidationMask, assetNameValidationMaskExample);
    if (!isValid) {
      throw new AssetNameValidationMaskDoesNotMatchExampleException();
    }
  }
}
