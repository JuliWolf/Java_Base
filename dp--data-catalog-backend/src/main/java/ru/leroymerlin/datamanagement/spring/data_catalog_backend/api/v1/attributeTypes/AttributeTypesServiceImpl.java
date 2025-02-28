package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.models.AttributeTypeAllowedValueWithAttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.models.AttributeTypeUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.AssetTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.RelationTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.AttributeTypesAllowedValuesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeValueAlreadyAssignedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.AttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PatchAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAllowedValueResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.RoleActionsDAO;

/**
 * @author JuliWolf
 */
@Service
public class AttributeTypesServiceImpl extends AttributeTypesDAO implements AttributeTypesService {
  private final RoleActionCachingService roleActionCachingService;

  private final LanguageService languageService;

  private final AttributeRepository attributeRepository;

  private final AttributeTypesAllowedValuesDAO attributeTypesAllowedValuesDAO;
  private final AttributesDAO attributesDAO;
  private final RoleActionsDAO roleActionsDAO;
  private final RelationAttributesDAO relationAttributesDAO;
  private final RelationComponentAttributesDAO relationComponentAttributesDAO;
  private final AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO;
  private final RelationTypeAttributeTypesAssignmentsDAO relationTypeAttributeTypesAssignmentsDAO;
  private final RelationTypeComponentAttributeTypesAssignmentsDAO relationTypeComponentAttributeTypesAssignmentsDAO;
  private final AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO;

  @Autowired
  public AttributeTypesServiceImpl (
    AttributeTypeRepository attributeTypeRepository,
    RoleActionCachingService roleActionCachingService,
    LanguageService languageService,
    AttributeRepository attributeRepository,
    AttributeTypesAllowedValuesDAO attributeTypesAllowedValuesDAO,
    AttributesDAO attributesDAO,
    RoleActionsDAO roleActionsDAO,
    RelationAttributesDAO relationAttributesDAO,
    RelationComponentAttributesDAO relationComponentAttributesDAO,
    AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO,
    RelationTypeAttributeTypesAssignmentsDAO relationTypeAttributeTypesAssignmentsDAO,
    RelationTypeComponentAttributeTypesAssignmentsDAO relationTypeComponentAttributeTypesAssignmentsDAO,
    AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO
  ) {
    super(attributeTypeRepository);

    this.roleActionCachingService = roleActionCachingService;
    this.languageService = languageService;
    this.attributeRepository = attributeRepository;
    this.attributeTypesAllowedValuesDAO = attributeTypesAllowedValuesDAO;
    this.attributesDAO = attributesDAO;
    this.roleActionsDAO = roleActionsDAO;
    this.relationAttributesDAO = relationAttributesDAO;
    this.relationComponentAttributesDAO = relationComponentAttributesDAO;
    this.assetTypeAttributeTypesAssignmentsDAO = assetTypeAttributeTypesAssignmentsDAO;
    this.relationTypeAttributeTypesAssignmentsDAO = relationTypeAttributeTypesAssignmentsDAO;
    this.relationTypeComponentAttributeTypesAssignmentsDAO = relationTypeComponentAttributeTypesAssignmentsDAO;
    this.assetTypeCardHeaderAssignmentDAO = assetTypeCardHeaderAssignmentDAO;
  }

  @Override
  @Transactional
  public PostAttributeTypeResponse createAttributeType (
    PostAttributeTypeRequest attributeTypeRequest, User user
  ) throws AttributeTypeValueAlreadyAssignedException {
    Language ru = languageService.getLanguage("ru");

    boolean isList = (
      attributeTypeRequest.getAttribute_type_kind().equals(AttributeKindType.SINGLE_VALUE_LIST) ||
      attributeTypeRequest.getAttribute_type_kind().equals(AttributeKindType.MULTIPLE_VALUE_LIST)
    );

    if (isList && hasDuplicatedValues(attributeTypeRequest.getAttribute_type_allowed_values())) {
      throw new AttributeTypeValueAlreadyAssignedException();
    }

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType(
      attributeTypeRequest.getAttribute_type_name(),
      attributeTypeRequest.getAttribute_type_description(),
      attributeTypeRequest.getAttribute_type_kind(),
      attributeTypeRequest.getValidation_mask(),
      attributeTypeRequest.getRdm_table_id(),
      ru,
      user
    ));

    // Clear role actions cache is someone tried to get attribute type with this id
    roleActionCachingService.evictByValueInKey(attributeType.getAttributeTypeId().toString());

    List<PostAllowedValueResponse> allowedValueList = new ArrayList<>();

    if (isList) {
      allowedValueList = prepareAllowedValues(attributeTypeRequest, attributeType, ru, user);
    }

    return new PostAttributeTypeResponse(
      attributeType.getAttributeTypeId(),
      attributeType.getAttributeTypeName(),
      attributeType.getAttributeTypeDescription(),
      attributeType.getAttributeKindType(),
      attributeType.getValidationMask(),
      ru.getLanguage(),
      attributeType.getRdmTableId(),
      new Timestamp(System.currentTimeMillis()),
      user.getUserId(),
      allowedValueList
    );
  }

  @Override
  public AttributeTypeResponse updateAttributeType (
    UUID attributeTypeId, PatchAttributeTypeRequest attributeTypeRequest, User user
  ) throws
    AttributeTypeNotFoundException,
    IncompatibleAttributeKindException,
    ValidationMaskCantBeAppliedException,
    AttributeDoesNotMatchTheMaskException
  {
    AttributeType foundAttributeType = findAttributeTypeById(attributeTypeId, false);

    OptionalUtils.doActionIfPresent(attributeTypeRequest.getAttribute_type_name(), name -> foundAttributeType.setAttributeTypeName(name.orElse(foundAttributeType.getAttributeTypeName())));
    OptionalUtils.doActionIfPresent(attributeTypeRequest.getAttribute_type_description(), description -> foundAttributeType.setAttributeTypeDescription(description.orElse(null)));
    OptionalUtils.doActionIfPresent(attributeTypeRequest.getRdm_table_id(), rdmTableId -> foundAttributeType.setRdmTableId(rdmTableId.orElse(null)));

    validateAttributes(foundAttributeType, attributeTypeRequest);

    OptionalUtils.doActionIfPresent(attributeTypeRequest.getValidation_mask(), validationMask -> foundAttributeType.setValidationMask(validationMask.orElse(null)));

    if (attributeTypeRequest.getAttribute_kind() != null) {
      updateAttributeKindType(foundAttributeType, attributeTypeRequest.getAttribute_kind());
    }

    foundAttributeType.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    foundAttributeType.setModifiedBy(user);

    AttributeType attributeType = attributeTypeRepository.save(foundAttributeType);
    List<AttributeTypeAllowedValueWithAttributeType> attributeTypeAllowedValues = attributeTypesAllowedValuesDAO.findAllByAttributeTypeId(List.of(foundAttributeType.getAttributeTypeId()));

    return getAttributeTypeResponse(attributeType, attributeTypeAllowedValues);
  }

  @Override
  public GetAttributeTypeResponse getAttributeTypeById (UUID attributeTypeId) throws AttributeTypeNotFoundException {
    AttributeType attributeType = findAttributeTypeById(attributeTypeId, true);
    List<AttributeTypeUsageCount> attributeTypeUsageCounts = attributeTypeRepository.countAttributeTypesUsage(List.of(attributeType.getAttributeTypeId()));
    List<AttributeTypeAllowedValueWithAttributeType> attributeTypeAllowedValues = attributeTypesAllowedValuesDAO.findAllByAttributeTypeId(List.of(attributeType.getAttributeTypeId()));

    long count = attributeTypeUsageCounts.isEmpty() ? 0L : attributeTypeUsageCounts.get(0).getCount();
    return getAttributeTypeResponse(attributeType, attributeTypeAllowedValues, count);
  }

  @Override
  public GetAttributeTypesResponse getAttributeTypeByParams (
    String attributeTypeName,
    String attributeTypeDescription,
    AttributeKindType attributeKind,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AttributeType> attributeTypes = attributeTypeRepository.findAllByParamsPageable(
      attributeTypeName,
      attributeTypeDescription,
      attributeKind,
      PageRequest.of(pageNumber, pageSize, Sort.by("attributeTypeName").ascending())
    );

    List<UUID> attributeTypeIds = attributeTypes.stream().map(AttributeType::getAttributeTypeId).toList();

    List<AttributeTypeUsageCount> attributeTypeUsageCounts = attributeTypeRepository.countAttributeTypesUsage(attributeTypeIds);
    Map<UUID, Long> countByAttributeType = attributeTypeUsageCounts.stream().collect(Collectors.toMap(
      AttributeTypeUsageCount::getAttributeTypeId,
      AttributeTypeUsageCount::getCount
    ));

    List<AttributeTypeAllowedValueWithAttributeType> attributeTypeValues = attributeTypesAllowedValuesDAO.findAllByAttributeTypeId(attributeTypeIds);
    Map<UUID, List<AttributeTypeAllowedValueWithAttributeType>> valuesByAttributeTypeId = attributeTypeValues.stream().collect(Collectors.groupingBy(AttributeTypeAllowedValueWithAttributeType::getAttributeTypeId));

    List<GetAttributeTypeResponse> attributeTypeResponse = attributeTypes.stream()
      .map(response -> getAttributeTypeResponse(
        response,
        valuesByAttributeTypeId.getOrDefault(response.getAttributeTypeId(), new ArrayList<>()),
        countByAttributeType.getOrDefault(response.getAttributeTypeId(), 0L))
      )
      .toList();

    return new GetAttributeTypesResponse(
      attributeTypes.getTotalElements(),
      pageSize,
      pageNumber,
      attributeTypeResponse
    );
  }

  @Override
  @Transactional
  public void deleteAttributeTypeById (
    UUID attributeTypeId, User user
  ) throws
    AttributeTypeNotFoundException,
    AttributeWithAttributeTypeExistsException,
    RelationAttributeWithAttributeTypeExistsException,
    RelationComponentAttributeWithAttributeTypeExistsException
  {
    AttributeType attributeType = findAttributeTypeById(attributeTypeId, true);

    Boolean isAttributesExists = attributesDAO.isAttributesExistsByAttributeType(attributeTypeId);
    if (isAttributesExists) {
      throw new AttributeWithAttributeTypeExistsException();
    }

    Boolean isRelationAttributesExists = relationAttributesDAO.isRelationAttributesExistsByAttributeType(attributeTypeId);
    if (isRelationAttributesExists) {
      throw new RelationAttributeWithAttributeTypeExistsException();
    }

    Boolean isRelationComponentAttributesExists = relationComponentAttributesDAO.isRelationComponentAttributesExistsByAttributeType(attributeTypeId);
    if (isRelationComponentAttributesExists) {
      throw new RelationComponentAttributeWithAttributeTypeExistsException();
    }

    roleActionsDAO.deleteAllByParams(null, null, attributeTypeId, null, user);
    attributeTypesAllowedValuesDAO.deleteAllByAttributeTypeId(attributeTypeId, user);
    assetTypeCardHeaderAssignmentDAO.deleteAssetTypeCardHeaderAssignmentByParams(null, null, attributeTypeId, user);
    assetTypeAttributeTypesAssignmentsDAO.deleteAllByParams(null, attributeTypeId, user);
    relationTypeAttributeTypesAssignmentsDAO.deleteAllByAttributeTypeId(attributeTypeId, user);
    relationTypeComponentAttributeTypesAssignmentsDAO.deleteAllByAttributeTypeId(attributeTypeId, user);

    attributeType.setIsDeleted(true);
    attributeType.setDeletedBy(user);
    attributeType.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    attributeTypeRepository.save(attributeType);

    roleActionCachingService.evictByValueInKey(attributeTypeId.toString());
  }

  private void validateAttributes (
    AttributeType attributeType,
    PatchAttributeTypeRequest request
  ) throws AttributeDoesNotMatchTheMaskException, ValidationMaskCantBeAppliedException {
    Optional<String> optionalValidationMask = OptionalUtils.getOptionalFromField(request.getValidation_mask());

    boolean isOldAttributeKindTypeIsText = attributeType.getAttributeKindType().equals(AttributeKindType.TEXT);
    boolean isNewAttributeKindTypeIsText = (
      request.getAttribute_kind() != null &&
      request.getAttribute_kind().equals(AttributeKindType.TEXT)
    );

    if (optionalValidationMask.isPresent() && !isNewAttributeKindTypeIsText && !isOldAttributeKindTypeIsText) {
      throw new ValidationMaskCantBeAppliedException();
    }

    if (!isNewAttributeKindTypeIsText && optionalValidationMask.isEmpty()) return;

    List<Attribute> attributes = attributeRepository.findAllByAttributeTypeId(attributeType.getAttributeTypeId());
    String validationMask = optionalValidationMask.orElseGet(attributeType::getValidationMask);

    attributes.forEach(attribute -> {
      if (!Pattern.matches(validationMask, attribute.getValue())) {
        throw new AttributeDoesNotMatchTheMaskException();
      }
    });
  }

  private List<PostAllowedValueResponse> prepareAllowedValues (
    PostAttributeTypeRequest attributeTypeRequest,
    AttributeType attributeType,
    Language ru,
    User user
  ) {
    return attributeTypeRequest.getAttribute_type_allowed_values()
      .stream()
      .map(value -> {
        AttributeTypeAllowedValue allowedValue = attributeTypesAllowedValuesDAO
          .saveAttributeTypeAllowedValue(new AttributeTypeAllowedValue(
              attributeType,
              value,
              ru,
              user
            )
          );

        return new PostAllowedValueResponse(
          allowedValue.getValueId(),
          allowedValue.getValue(),
          new Timestamp(System.currentTimeMillis()),
          user.getUserId()
        );
      }).toList();
  }

  private void updateAttributeKindType (
    AttributeType attributeType,
    AttributeKindType attributeKindType
  ) throws IncompatibleAttributeKindException {
    if (
      attributeKindType.equals(AttributeKindType.DECIMAL) &&
      attributeType.getAttributeKindType().equals(AttributeKindType.INTEGER)
    ) {
      attributeType.setAttributeKindType(attributeKindType);

      return;
    }

    if (
      attributeKindType.equals(AttributeKindType.RTF) &&
      attributeType.getAttributeKindType().equals(AttributeKindType.TEXT)
    ) {
      attributeType.setValidationMask(null);
      attributeType.setAttributeKindType(attributeKindType);

      return;
    }

    if (
      attributeKindType.equals(AttributeKindType.MULTIPLE_VALUE_LIST) &&
      attributeType.getAttributeKindType().equals(AttributeKindType.SINGLE_VALUE_LIST)
    ) {
      attributeType.setAttributeKindType(attributeKindType);

      return;
    }

    if (
      attributeKindType.equals(AttributeKindType.DATE_TIME) &&
      attributeType.getAttributeKindType().equals(AttributeKindType.DATE)
    ) {
      attributeType.setAttributeKindType(attributeKindType);

      return;
    }

    if (
      attributeKindType.equals(AttributeKindType.TEXT) &&
      !attributeType.getAttributeKindType().equals(AttributeKindType.RTF) && !attributeType.getAttributeKindType().equals(AttributeKindType.BOOLEAN_ICON)
    ) {
      attributeType.setAttributeKindType(attributeKindType);

      return;
    }

    if (
      !attributeKindType.equals(AttributeKindType.SINGLE_VALUE_LIST) ||
      !attributeType.getAttributeKindType().equals(AttributeKindType.MULTIPLE_VALUE_LIST)
    ) {
      throw new IncompatibleAttributeKindException();
    }

    if (attributeTypesAllowedValuesDAO.countAttributeTypeAllowedValueByAttributeTypeId(attributeType.getAttributeTypeId()) > 1) {
      throw new IncompatibleAttributeKindException();
    }

    attributeType.setAttributeKindType(attributeKindType);
  }

  private GetAttributeTypeResponse getAttributeTypeResponse (
    AttributeType attributeType,
    List<AttributeTypeAllowedValueWithAttributeType> attributeTypeAllowedValues,
    Long attributeTypeUsageCount
  ) {
    AttributeTypeResponse attributeTypeResponse = getAttributeTypeResponse(attributeType, attributeTypeAllowedValues);

    return new GetAttributeTypeResponse(attributeTypeResponse, attributeTypeUsageCount);
  }

  private AttributeTypeResponse getAttributeTypeResponse (
    AttributeType attributeType,
    List<AttributeTypeAllowedValueWithAttributeType> attributeTypeAllowedValues
  ) {
    UUID lastModifiedBy = attributeType.getModifiedBy() != null ? attributeType.getModifiedBy().getUserId() : null;
    List<PostAllowedValueResponse> postAllowedValueResponses = mapAllowedValues(attributeTypeAllowedValues);

    return new AttributeTypeResponse(
      attributeType.getAttributeTypeId(),
      attributeType.getAttributeTypeName(),
      attributeType.getAttributeTypeDescription(),
      attributeType.getAttributeKindType(),
      attributeType.getValidationMask(),
      attributeType.getLanguageName(),
      attributeType.getRdmTableId(),
      attributeType.getCreatedOn(),
      attributeType.getCreatedByUUID(),
      attributeType.getLastModifiedOn(),
      lastModifiedBy,
      postAllowedValueResponses
    );
  }

  private List<PostAllowedValueResponse> mapAllowedValues (List<AttributeTypeAllowedValueWithAttributeType> values) {
    return values.stream()
      .sorted(Comparator.comparing(AttributeTypeAllowedValueWithAttributeType::getValue))
      .map(value -> new PostAllowedValueResponse(
        value.getAttributeTypeAllowedValueId(),
        value.getValue(),
        value.getCreatedOn(),
        value.getCreatedBy()
      )).toList();
  }

  private boolean hasDuplicatedValues (List<String> values) {
    Set<String> set = new HashSet<>(values);

    return set.size() < values.size();
  }
}
