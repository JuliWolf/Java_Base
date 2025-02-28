package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.AttributeValueValidator;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.models.RelationComponentAttributeWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributesHistory.RelationComponentAttributesHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeTypeNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.AttributeTypeNotAllowedForRelationComponentException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.RelationComponentAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationComponentNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class RelationComponentAttributesServiceImpl extends RelationComponentAttributesDAO implements RelationComponentAttributesService {
  private final RelationComponentAttributesHistoryRepository relationComponentAttributesHistoryRepository;

  private final RelationsDAO relationsDAO;

  private final AttributeTypesDAO attributeTypesDAO;

  private final RelationTypeComponentAttributeTypesAssignmentsDAO relationTypeComponentAttributeTypesAssignmentsDAO;

  private final AttributeValueValidator attributeValueValidator;

  private final LanguageService languageService;

  public RelationComponentAttributesServiceImpl (
    RelationComponentAttributesHistoryRepository relationComponentAttributesHistoryRepository,
    RelationsDAO relationsDAO,
    AttributeTypesDAO attributeTypesDAO,
    RelationTypeComponentAttributeTypesAssignmentsDAO relationTypeComponentAttributeTypesAssignmentsDAO,
    AttributeValueValidator attributeValueValidator,
    LanguageService languageService
  ) {
    this.relationComponentAttributesHistoryRepository = relationComponentAttributesHistoryRepository;

    this.relationsDAO = relationsDAO;
    this.attributeTypesDAO = attributeTypesDAO;
    this.relationTypeComponentAttributeTypesAssignmentsDAO = relationTypeComponentAttributeTypesAssignmentsDAO;
    this.attributeValueValidator = attributeValueValidator;
    this.languageService = languageService;
  }

  @Override
  public PostRelationComponentAttributeResponse createRelationComponentAttribute (
    PostRelationComponentAttributeRequest request,
    User user
  ) throws
    AttributeTypeNotFoundException,
    AttributeTypeNotAllowedException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    RelationComponentNotFoundException,
    AttributeValueMaskValidationException,
    AttributeTypeNotAllowedForRelationComponentException
  {
    AttributeType attributeType = attributeTypesDAO.findAttributeTypeById(UUID.fromString(request.getAttribute_type_id()), false);
    RelationComponent relationComponent = relationsDAO.findRelationComponentById(UUID.fromString(request.getRelation_component_id()));

    checkIfRelationTypeComponentAttributeTypeAssignmentsExists(relationComponent.getRelationTypeComponent().getRelationTypeComponentId(), attributeType.getAttributeTypeId());

    attributeValueValidator.validateValueType(request.getValue(), attributeType.getAttributeTypeId(), attributeType.getValidationMask(), attributeType.getAttributeKindType());

    Language language = languageService.getLanguage("ru");

    RelationComponentAttribute relationComponentAttribute = new RelationComponentAttribute(
      attributeType,
      relationComponent,
      language,
      user
    );

    attributeValueValidator.setAttributeValueByType(relationComponentAttribute, request.getValue(), attributeType.getAttributeKindType());

    RelationComponentAttribute createdRelationComponentAttribute = relationComponentAttributeRepository.save(relationComponentAttribute);
    createRelationAttributeComponentHistory(createdRelationComponentAttribute, MethodType.POST);

    return new PostRelationComponentAttributeResponse(
      createdRelationComponentAttribute.getRelationComponentAttributeId(),
      createdRelationComponentAttribute.getAttributeType().getAttributeTypeId(),
      createdRelationComponentAttribute.getRelationComponent().getRelationComponentId(),
      createdRelationComponentAttribute.getValue(),
      createdRelationComponentAttribute.getIsInteger(),
      createdRelationComponentAttribute.getValueNumeric(),
      createdRelationComponentAttribute.getValueBoolean(),
      createdRelationComponentAttribute.getValueDatetime(),
      createdRelationComponentAttribute.getLanguageName(),
      createdRelationComponentAttribute.getCreatedOn(),
      createdRelationComponentAttribute.getCreatedByUUID()
    );
  }

  @Override
  @Transactional
  public PatchRelationComponentAttributeResponse updateRelationComponentAttribute (UUID relationComponentAttributeId, PatchRelationComponentAttributeRequest request, User user) throws AttributeInvalidDataTypeException, AttributeValueNotAllowedException, AttributeValueMaskValidationException, RelationComponentAttributeNotFoundException {
    RelationComponentAttribute relationComponentAttribute = findRelationComponentAttributeById(relationComponentAttributeId, true);
    AttributeType attributeType = relationComponentAttribute.getAttributeType();

    attributeValueValidator.validateValueType(request.getValue(), attributeType.getAttributeTypeId(), attributeType.getValidationMask(), attributeType.getAttributeKindType());

    attributeValueValidator.setAttributeValueByType(relationComponentAttribute, request.getValue(), attributeType.getAttributeKindType());

    relationComponentAttribute.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    relationComponentAttribute.setModifiedBy(user);

    relationComponentAttribute = relationComponentAttributeRepository.save(relationComponentAttribute);
    createRelationAttributeComponentHistory(relationComponentAttribute, MethodType.PATCH);

    return new PatchRelationComponentAttributeResponse(
      relationComponentAttribute.getRelationComponentAttributeId(),
      attributeType.getAttributeTypeId(),
      relationComponentAttribute.getRelationComponent().getRelationComponentId(),
      relationComponentAttribute.getValue(),
      relationComponentAttribute.getIsInteger(),
      relationComponentAttribute.getValueNumeric(),
      relationComponentAttribute.getValueBoolean(),
      relationComponentAttribute.getValueDatetime(),
      relationComponentAttribute.getLanguageName(),
      relationComponentAttribute.getCreatedOn(),
      relationComponentAttribute.getCreatedByUUID(),
      relationComponentAttribute.getLastModifiedOn(),
      user.getUserId()
    );
  }

  @Override
  public GetRelationComponentAttributeResponse getRelationComponentAttributeById (UUID relationComponentAttributeId) throws RelationComponentAttributeNotFoundException {
    Optional<RelationComponentAttributeWithConnectedValues> optionalRelationComponentAttribute = relationComponentAttributeRepository.findRelationComponentAttributeById(relationComponentAttributeId);

    if (optionalRelationComponentAttribute.isEmpty()) {
      throw new RelationComponentAttributeNotFoundException();
    }

    RelationComponentAttributeWithConnectedValues relationAttribute = optionalRelationComponentAttribute.get();

    return new GetRelationComponentAttributeResponse(relationAttribute);
  }

  @Override
  public GetRelationComponentAttributesResponse getRelationComponentAttributesByParams (
    List<UUID> attributeTypeIds,
    List<UUID> relationComponentIds,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<RelationComponentAttributeWithConnectedValues> responses = relationComponentAttributeRepository.findRelationComponentAttributesByParamsPageable(
      attributeTypeIds != null ? attributeTypeIds.size() : 0,
      attributeTypeIds,
      relationComponentIds != null ? relationComponentIds.size() : 0,
      relationComponentIds,
      PageRequest.of(pageNumber, pageSize, Sort.by("at.attributeTypeName").ascending())
    );

    List<GetRelationComponentAttributeResponse> relationComponentAttributes = responses.stream().map(GetRelationComponentAttributeResponse::new).toList();

    return new GetRelationComponentAttributesResponse(
      responses.getTotalElements(),
      pageSize,
      pageNumber,
      relationComponentAttributes
    );
  }

  @Override
  @Transactional
  public void deleteRelationComponentAttributeById (UUID relationComponentAttributeId, User user) throws RelationComponentAttributeNotFoundException {
    RelationComponentAttribute relationComponentAttribute = findRelationComponentAttributeById(relationComponentAttributeId, false);

    relationComponentAttribute.setIsDeleted(true);
    relationComponentAttribute.setDeletedBy(user);
    relationComponentAttribute.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    relationComponentAttributeRepository.save(relationComponentAttribute);

    createRelationAttributeComponentHistory(relationComponentAttribute, MethodType.DELETE);
  }

  private void checkIfRelationTypeComponentAttributeTypeAssignmentsExists (UUID relationTypeComponentId, UUID attributeTypeId) throws AttributeTypeNotAllowedForRelationComponentException {
    Boolean isExists = relationTypeComponentAttributeTypesAssignmentsDAO.isAssignmentsExistsByRelationTypeComponentAndAttributeType(relationTypeComponentId, attributeTypeId);

    if (!isExists) {
      throw new AttributeTypeNotAllowedForRelationComponentException();
    }
  }

  private void createRelationAttributeComponentHistory (RelationComponentAttribute relationComponentAttribute, MethodType methodType) {
    RelationComponentAttributeHistory relationComponentAttributeHistory = new RelationComponentAttributeHistory(relationComponentAttribute);

    switch (methodType) {
      case POST -> relationComponentAttributeHistory.setCreatedValidDate();
      case PATCH -> {
        relationComponentAttributeHistory.setUpdatedValidDate();
        relationComponentAttributesHistoryRepository.updateLastRelationComponentAttributeHistory(
          relationComponentAttribute.getLastModifiedOn(),
          relationComponentAttribute.getRelationComponentAttributeId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
      case DELETE -> {
        relationComponentAttributeHistory.setDeletedValidDate();

        relationComponentAttributesHistoryRepository.updateLastRelationComponentAttributeHistory(
          relationComponentAttribute.getDeletedOn(),
          relationComponentAttribute.getRelationComponentAttributeId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
    }

    relationComponentAttributesHistoryRepository.save(relationComponentAttributeHistory);
  }
}
