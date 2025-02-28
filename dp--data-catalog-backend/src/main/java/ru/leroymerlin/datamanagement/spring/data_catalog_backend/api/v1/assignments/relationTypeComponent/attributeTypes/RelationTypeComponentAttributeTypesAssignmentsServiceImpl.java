package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAttributeTypeAssignmentUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.models.RelationTypeComponentAttributeTypeAssignmentWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponentAttributeTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.SortUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.AttributeTypeIsUsedForRelationComponentAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.RelationTypeComponentAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypeComponents.RelationTypeComponentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class RelationTypeComponentAttributeTypesAssignmentsServiceImpl extends RelationTypeComponentAttributeTypesAssignmentsDAO implements RelationTypeComponentAttributeTypesAssignmentsService {
  private final RelationTypeComponentsDAO relationTypeComponentsDAO;

  private final AttributeTypesDAO attributeTypesDAO;

  private final RelationComponentAttributesDAO relationComponentAttributesDAO;

  public RelationTypeComponentAttributeTypesAssignmentsServiceImpl (
    RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository,
    RelationTypeComponentsDAO relationTypeComponentsDAO,
    AttributeTypesDAO attributeTypesDAO,
    RelationComponentAttributesDAO relationComponentAttributesDAO
  ) {
    super(relationTypeComponentAttributeTypeAssignmentRepository);

    this.relationTypeComponentsDAO = relationTypeComponentsDAO;
    this.attributeTypesDAO = attributeTypesDAO;
    this.relationComponentAttributesDAO = relationComponentAttributesDAO;
  }

  @Override
  @Transactional
  public PostRelationTypeComponentAttributeTypesResponse createRelationTypeComponentAttributeTypesAssignments (
    UUID relationTypeComponentId,
    PostRelationTypeComponentAttributeTypesRequest assignmentsRequest,
    User user
  ) throws RelationTypeComponentNotFoundException {
    RelationTypeComponent relationTypeComponent = relationTypeComponentsDAO.findRelationTypeComponentById(relationTypeComponentId, false);

    List<PostRelationTypeComponentAttributeTypeResponse> attributeTypesAssignments = assignmentsRequest.getRelation_type_component_attribute_type_assignment()
      .stream()
      .map(assignment -> {
        AttributeType attributeType = attributeTypesDAO.findAttributeTypeById(UUID.fromString(assignment.getAttribute_type_id()), false);

        RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(
          relationTypeComponent,
          attributeType,
          user
        ));

        return new PostRelationTypeComponentAttributeTypeResponse(
          relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId(),
          relationTypeComponent.getRelationTypeComponentId(),
          attributeType.getAttributeTypeId(),
          relationTypeComponentAttributeTypeAssignment.getCreatedOn(),
          relationTypeComponentAttributeTypeAssignment.getCreatedByUUID()
        );
      }).toList();

    return new PostRelationTypeComponentAttributeTypesResponse(attributeTypesAssignments);
  }

  @Override
  public GetRelationTypeComponentAttributeTypesResponse getRelationTypeComponentAttributeTypesAssignments (
    UUID relationTypeComponentId
  ) throws RelationTypeComponentNotFoundException {
    RelationTypeComponent relationTypeComponent = relationTypeComponentsDAO.findRelationTypeComponentById(relationTypeComponentId, false);

    List<RelationTypeComponentAttributeTypeAssignmentWithConnectedValues> assignments = relationTypeComponentAttributeTypeAssignmentRepository.findAllWithJoinedTablesByRelationTypeComponentId(
      relationTypeComponentId
    );

    List<GetRelationTypeComponentAttributeTypeResponse> responses = assignments.stream()
      .map(assignment -> new GetRelationTypeComponentAttributeTypeResponse(
        assignment.getRelationTypeComponentAttributeTypeId(),
        assignment.getAttributeTypeId(),
        assignment.getAttributeTypeName(),
        assignment.getAttributeKind(),
        assignment.getValidationMask(),
        assignment.getAllowedValues(),
        assignment.getCreatedOn(),
        assignment.getCreatedBy()
      )).toList();


    return new GetRelationTypeComponentAttributeTypesResponse(
      relationTypeComponent.getRelationTypeComponentId(),
      relationTypeComponent.getRelationTypeComponentName(),
      responses
    );
  }

  @Override
  public GetRelationTypeComponentsAttributeTypesUsageCountResponse getRelationTypeComponentAttributeTypesWithUsageCount (
    GetRelationTypeComponentAssetTypesUsageCountParams params
  ) {
    Integer pageSize = PageableUtils.getPageSize(params.getPageSize());
    Integer pageNumber = PageableUtils.getPageNumber(params.getPageNumber());

    Page<RelationTypeComponentAttributeTypeAssignmentUsageCount> response = relationTypeComponentAttributeTypeAssignmentRepository.findAllWithUsageCountPageable(
      params.getAttributeTypeId(),
      params.getRelationTypeComponentId(),
      PageRequest.of(pageNumber, pageSize, getSorting(params.getSortField() == null ? SortField.RELATION_TYPE_COMPONENT_ATTRIBUTE_TYPE_USAGE_COUNT : params.getSortField(), params.getSortOrder()))
    );

    List<GetRelationTypeComponentsAttributeTypesUsageCountResponse.GetRelationTypeComponentsAttributeTypeResponse> attributeTypeResponses = response.stream().map(item -> new GetRelationTypeComponentsAttributeTypesUsageCountResponse.GetRelationTypeComponentsAttributeTypeResponse(
      item.getRelationTypeComponentAttributeTypeAssignmentId(),
      item.getRelationTypeComponentId(),
      item.getRelationTypeComponentName(),
      item.getAttributeTypeId(),
      item.getAttributeTypeName(),
      item.getCount(),
      item.getCreatedOn(),
      item.getCreatedBy()
    )).toList();

    return new GetRelationTypeComponentsAttributeTypesUsageCountResponse(
      response.getTotalElements(),
      pageSize,
      pageNumber,
      attributeTypeResponses
    );
  }

  @Override
  public void deleteRelationTypeComponentAttributeTypeAssignment (UUID relationTypeComponentAttributeTypeAssignmentId, User user) throws RelationTypeComponentAttributeTypeAssignmentNotFound {
    RelationTypeComponentAttributeTypeAssignment assignment = findRelationTypeComponentAttributeTypeAssignmentById(relationTypeComponentAttributeTypeAssignmentId);

    Boolean hasConnectedRelationComponentAttributes = relationComponentAttributesDAO.isRelationComponentAttributesExistsByRelationTypeComponentId(assignment.getRelationTypeComponent().getRelationTypeComponentId());
    if (hasConnectedRelationComponentAttributes) {
      throw new AttributeTypeIsUsedForRelationComponentAttributeException();
    }

    assignment.setIsDeleted(true);
    assignment.setDeletedBy(user);
    assignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    relationTypeComponentAttributeTypeAssignmentRepository.save(assignment);
  }

  private Sort getSorting (SortField sortField, SortOrder sortOrder) {
    return SortUtils.getSort(sortOrder, sortField.getValue(), SortField.RELATION_TYPE_COMPONENT_ATTRIBUTE_TYPE_USAGE_COUNT.getValue());
  }
}
