package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.AttributeValueValidator;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.models.RelationAttributeWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributesHistory.RelationAttributesHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.RelationTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.AttributeTypeNotAllowedForRelationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.RelationAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class RelationAttributesServiceImpl extends RelationAttributesDAO implements RelationAttributesService {
  private final RelationAttributesHistoryRepository relationAttributesHistoryRepository;

  private final RelationsDAO relationsDAO;

  private final AttributeTypesDAO attributeTypesDAO;

  private final RelationTypeAttributeTypesAssignmentsDAO relationTypeAttributeTypesAssignmentsDAO;

  private final AttributeValueValidator attributeValueValidator;

  private final LanguageService languageService;

  @Autowired
  public RelationAttributesServiceImpl (
    RelationAttributesHistoryRepository relationAttributesHistoryRepository,
    RelationsDAO relationsDAO,
    AttributeTypesDAO attributeTypesDAO,
    RelationTypeAttributeTypesAssignmentsDAO relationTypeAttributeTypesAssignmentsDAO,
    AttributeValueValidator attributeValueValidator,
    LanguageService languageService
  ) {
    this.relationAttributesHistoryRepository = relationAttributesHistoryRepository;

    this.relationsDAO = relationsDAO;
    this.attributeTypesDAO = attributeTypesDAO;
    this.relationTypeAttributeTypesAssignmentsDAO = relationTypeAttributeTypesAssignmentsDAO;
    this.attributeValueValidator = attributeValueValidator;
    this.languageService = languageService;
  }

  @Override
  public PostRelationAttributeResponse createRelationAttribute (
    PostRelationAttributeRequest request,
    User user
  ) throws
    RelationNotFoundException,
    AttributeTypeNotFoundException,
    AttributeValueNotAllowedException,
    AttributeTypeNotAllowedForRelationException
  {
    Relation relation = relationsDAO.findRelationById(UUID.fromString(request.getRelation_id()));
    AttributeType attributeType = attributeTypesDAO.findAttributeTypeById(UUID.fromString(request.getAttribute_type_id()), false);

    checkIfRelationTypeAttributeTypeAssignmentsExists(relation.getRelationType().getRelationTypeId(), attributeType.getAttributeTypeId());

    attributeValueValidator.validateValueType(request.getValue(), attributeType.getAttributeTypeId(), attributeType.getValidationMask(), attributeType.getAttributeKindType());

    Language language = languageService.getLanguage("ru");

    RelationAttribute relationAttribute = new RelationAttribute(
      attributeType,
      relation,
      language,
      user
    );

    attributeValueValidator.setAttributeValueByType(relationAttribute, request.getValue(), attributeType.getAttributeKindType());

    RelationAttribute createdRelationAttribute = relationAttributeRepository.save(relationAttribute);
    createRelationAttributeHistory(createdRelationAttribute, MethodType.POST);

    return new PostRelationAttributeResponse(
      createdRelationAttribute.getRelationAttributeId(),
      attributeType.getAttributeTypeId(),
      relation.getRelationId(),
      relationAttribute.getValue(),
      relationAttribute.getIsInteger(),
      relationAttribute.getValueNumeric(),
      relationAttribute.getValueBoolean(),
      relationAttribute.getValueDatetime(),
      language.getLanguage(),
      relationAttribute.getCreatedOn(),
      relationAttribute.getCreatedByUUID()
    );
  }

  @Override
  @Transactional
  public PatchRelationAttributeResponse updateRelationAttribute (
    UUID relationAttributeId,
    PatchRelationAttributeRequest patchRelationAttributeRequest,
    User user
  ) throws
    AttributeTypeNotFoundException,
    AttributeValueNotAllowedException,
    RelationAttributeNotFoundException,
    AttributeTypeNotAllowedForRelationException
  {
    RelationAttribute relationAttribute = findRelationAttributeById(relationAttributeId, true);
    AttributeType attributeType = relationAttribute.getAttributeType();

    attributeValueValidator.validateValueType(patchRelationAttributeRequest.getValue(), attributeType.getAttributeTypeId(), attributeType.getValidationMask(), attributeType.getAttributeKindType());

    attributeValueValidator.setAttributeValueByType(relationAttribute, patchRelationAttributeRequest.getValue(), attributeType.getAttributeKindType());

    relationAttribute.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    relationAttribute.setModifiedBy(user);

    relationAttribute = relationAttributeRepository.save(relationAttribute);

    createRelationAttributeHistory(relationAttribute, MethodType.PATCH);

    return new PatchRelationAttributeResponse(
      relationAttribute.getRelationAttributeId(),
      attributeType.getAttributeTypeId(),
      relationAttribute.getRelation().getRelationId(),
      relationAttribute.getValue(),
      relationAttribute.getIsInteger(),
      relationAttribute.getValueNumeric(),
      relationAttribute.getValueBoolean(),
      relationAttribute.getValueDatetime(),
      relationAttribute.getLanguageName(),
      relationAttribute.getCreatedOn(),
      relationAttribute.getCreatedByUUID(),
      relationAttribute.getLastModifiedOn(),
      user.getUserId()
    );
  }

  @Override
  public GetRelationAttributeResponse getRelationAttributeById (UUID relationAttributeId) throws RelationAttributeNotFoundException {
    Optional<RelationAttributeWithConnectedValues> optionalRelationAttribute = relationAttributeRepository.findRelationAttributeById(relationAttributeId);

    if (optionalRelationAttribute.isEmpty()) {
      throw new RelationAttributeNotFoundException();
    }

    RelationAttributeWithConnectedValues relationAttribute = optionalRelationAttribute.get();

    return new GetRelationAttributeResponse(relationAttribute);
  }

  @Override
  public GetRelationAttributesResponse getRelationAttributesByParams (UUID relationId, List<UUID> attributeTypeIds, Integer pageNumber, Integer pageSize) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<RelationAttributeWithConnectedValues> responses = relationAttributeRepository.findRelationAttributesByParamsPageable(
      relationId,
      attributeTypeIds != null ? attributeTypeIds.size() : 0,
      attributeTypeIds,
      PageRequest.of(pageNumber, pageSize, Sort.by("at.attributeTypeName").ascending())
    );

    List<GetRelationAttributeResponse> relationAttributes = responses.stream().map(GetRelationAttributeResponse::new).toList();

    return new GetRelationAttributesResponse(
      responses.getTotalElements(),
      pageSize,
      pageNumber,
      relationAttributes
    );
  }

  @Override
  @Transactional
  public void deleteRelationAttributeById (UUID relationAttributeId, User user) throws RelationAttributeNotFoundException {
    RelationAttribute relationAttribute = findRelationAttributeById(relationAttributeId, false);

    relationAttribute.setIsDeleted(true);
    relationAttribute.setDeletedBy(user);
    relationAttribute.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    relationAttributeRepository.save(relationAttribute);

    createRelationAttributeHistory(relationAttribute, MethodType.DELETE);
  }

  private void checkIfRelationTypeAttributeTypeAssignmentsExists (UUID relationTypeId, UUID attributeTypeId) throws AttributeTypeNotAllowedForRelationException {
    Boolean isExists = relationTypeAttributeTypesAssignmentsDAO.isAssignmentsExistsByRelationTypeAndAttributeType(relationTypeId, attributeTypeId);

     if (!isExists) {
       throw new AttributeTypeNotAllowedForRelationException();
     }
  }

  private void createRelationAttributeHistory (RelationAttribute relationAttribute, MethodType methodType) {
    RelationAttributeHistory relationAttributeHistory = new RelationAttributeHistory(relationAttribute);

    switch (methodType) {
      case POST -> relationAttributeHistory.setCreatedValidDate();
      case PATCH -> {
        relationAttributeHistory.setUpdatedValidDate();
        relationAttributesHistoryRepository.updateLastRelationAttributeHistory(
          relationAttribute.getLastModifiedOn(),
          relationAttribute.getRelationAttributeId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
      case DELETE -> {
        relationAttributeHistory.setDeletedValidDate();

        relationAttributesHistoryRepository.updateLastRelationAttributeHistory(
          relationAttribute.getDeletedOn(),
          relationAttribute.getRelationAttributeId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
    }

    relationAttributesHistoryRepository.save(relationAttributeHistory);
  }
}
