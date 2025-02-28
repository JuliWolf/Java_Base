package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.models.RelationTypeComponentAttributeTypeAssignmentUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.models.RelationTypeAttributeTypeAssignmentWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeAttributeTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.SortUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.AttributeTypeIsUsedForRelationAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.RelationTypeAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.RelationTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class RelationTypeAttributeTypesAssignmentsServiceImpl extends RelationTypeAttributeTypesAssignmentsDAO implements RelationTypeAttributeTypesAssignmentsService {
  private final RelationTypesDAO relationTypesDAO;

  private final AttributeTypesDAO attributeTypesDAO;

  private final RelationAttributesDAO relationAttributesDAO;

  public RelationTypeAttributeTypesAssignmentsServiceImpl (
    RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository,

    RelationTypesDAO relationTypesDAO,
    AttributeTypesDAO attributeTypesDAO,
    RelationAttributesDAO relationAttributesDAO
  ) {
    super(relationTypeAttributeTypeAssignmentRepository);

    this.relationTypesDAO = relationTypesDAO;
    this.attributeTypesDAO = attributeTypesDAO;
    this.relationAttributesDAO = relationAttributesDAO;
  }

  @Override
  @Transactional
  public PostRelationTypeAttributeTypesResponse createRelationTypeAttributeTypesAssignments (
    UUID uuid,
    PostRelationTypeAttributeTypesRequest assignmentsRequest,
    User user
  ) throws RelationTypeNotFoundException, AttributeTypeNotFoundException, IllegalArgumentException {
    RelationType relationType = relationTypesDAO.findRelationTypeById(uuid, false);

    List<PostRelationTypeAttributeTypeResponse> assignments = assignmentsRequest.getAttribute_assignment()
      .stream()
      .map(assignment -> {
        AttributeType attributeType = attributeTypesDAO.findAttributeTypeById(UUID.fromString(assignment.getAttribute_id()), false);

        RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(
          relationType,
          attributeType,
          user
        ));

        return new PostRelationTypeAttributeTypeResponse(
          relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId(),
          relationType.getRelationTypeId(),
          attributeType.getAttributeTypeId(),
          new Timestamp(System.currentTimeMillis()),
          user.getUserId()
        );
      }).toList();

    return new PostRelationTypeAttributeTypesResponse(assignments);
  }

  @Override
  public GetRelationTypeAttributeTypesResponse getRelationTypeAttributeTypesAssignmentsByRelationTypeId (UUID relationTypeId) throws RelationTypeNotFoundException {
    RelationType relationType = relationTypesDAO.findRelationTypeById(relationTypeId, false);

    List<RelationTypeAttributeTypeAssignmentWithConnectedValues> assignments = relationTypeAttributeTypeAssignmentRepository.findAllByRelationTypeIdWithJoinedTables(
      relationTypeId
    );

    List<GetRelationTypeAttributeTypeResponse> relationTypeAttributeTypeResponses = assignments.stream()
      .map(assignment -> new GetRelationTypeAttributeTypeResponse(
        assignment.getRelationTypeAttributeTypeAssignmentId(),
        assignment.getAttributeTypeId(),
        assignment.getAttributeTypeName(),
        assignment.getCreatedOn(),
        assignment.getCreatedBy()
      )).toList();

    return new GetRelationTypeAttributeTypesResponse(
      relationType.getRelationTypeId(),
      relationType.getRelationTypeName(),
      relationTypeAttributeTypeResponses
    );
  }

  @Override
  public GetRelationTypesAttributeTypesUsageCountResponse getRelationTypeAttributeTypesWithUsageCount (
    GetRelationTypeAttributeTypesUsageCountParams params
  )
    throws
    RelationTypeNotFoundException,
    AttributeTypeNotFoundException
  {
    Integer pageSize = PageableUtils.getPageSize(params.getPageSize());
    Integer pageNumber = PageableUtils.getPageNumber(params.getPageNumber());

    Page<RelationTypeComponentAttributeTypeAssignmentUsageCount> response = relationTypeAttributeTypeAssignmentRepository.findAllWithUsageCountPageable(
      params.getAttributeTypeId(),
      params.getRelationTypeId(),
      PageRequest.of(pageNumber, pageSize, getSorting(params.getSortField() == null ? SortField.RELATION_TYPE_ATTRIBUTE_TYPE_USAGE_COUNT : params.getSortField(), params.getSortOrder()))
    );

    List<GetRelationTypesAttributeTypesUsageCountResponse.GetRelationTypesAttributeTypeUsageCountResponse> usageCountList = response.stream()
      .map(assignment -> new GetRelationTypesAttributeTypesUsageCountResponse.GetRelationTypesAttributeTypeUsageCountResponse(
        assignment.getRelationTypeAttributeTypeAssignmentId(),
        assignment.getRelationTypeId(),
        assignment.getRelationTypeName(),
        assignment.getAttributeTypeId(),
        assignment.getAttributeTypeName(),
        assignment.getCount(),
        assignment.getCreatedOn(),
        assignment.getCreatedBy()
      )).toList();

    return new GetRelationTypesAttributeTypesUsageCountResponse(
      response.getTotalElements(),
      pageSize,
      pageNumber,
      usageCountList
    );
  }

  @Override
  public void deleteRelationTypeAttributeTypeAssignment (
    UUID relationTypeAttributeTypeAssignmentId,
    User user
  ) throws RelationTypeAttributeTypeAssignmentNotFound {
    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = findRelationTypeAttributeTypeAssignmentById(relationTypeAttributeTypeAssignmentId, false);

    Boolean isRelationAttributeExists = relationAttributesDAO.isRelationAttributesExistsByRelationTypeId(relationTypeAttributeTypeAssignment.getRelationType().getRelationTypeId());
    if (isRelationAttributeExists) {
      throw new AttributeTypeIsUsedForRelationAttributeException();
    }

    relationTypeAttributeTypeAssignment.setIsDeleted(true);
    relationTypeAttributeTypeAssignment.setDeletedBy(user);
    relationTypeAttributeTypeAssignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    relationTypeAttributeTypeAssignmentRepository.save(relationTypeAttributeTypeAssignment);
  }

  private Sort getSorting (SortField sortField, SortOrder sortOrder) {
    return SortUtils.getSort(sortOrder, sortField.getValue(), SortField.RELATION_TYPE_ATTRIBUTE_TYPE_USAGE_COUNT.getValue());
  }
}
