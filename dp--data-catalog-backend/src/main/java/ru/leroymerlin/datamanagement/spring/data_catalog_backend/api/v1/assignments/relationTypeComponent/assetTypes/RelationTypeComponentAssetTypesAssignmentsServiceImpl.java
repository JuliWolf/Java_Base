package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAssetTypeAssignmentWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponentAssetTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.AssetsTypeIsUsedInRelationsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentIsInherited;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.get.GetRelationTypeComponentAssetTypeAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.get.GetRelationTypeComponentAssetTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypeComponents.RelationTypeComponentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsDAO;

/**
 * @author juliwolf
 */

@Service
public class RelationTypeComponentAssetTypesAssignmentsServiceImpl extends RelationTypeComponentAssetTypesAssignmentsDAO implements RelationTypeComponentAssetTypesAssignmentsService {

  private final RelationTypeComponentsDAO relationTypeComponentsDAO;

  private final AssetTypesDAO assetTypesDAO;

  private final RelationsDAO relationsDAO;

  public RelationTypeComponentAssetTypesAssignmentsServiceImpl (
    RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository,
    RelationTypeComponentsDAO relationTypeComponentsDAO,
    AssetTypesDAO assetTypesDAO,
    RelationsDAO relationsDAO
  ) {
    super(relationTypeComponentAssetTypeAssignmentRepository);

    this.relationTypeComponentsDAO = relationTypeComponentsDAO;
    this.assetTypesDAO = assetTypesDAO;
    this.relationsDAO = relationsDAO;
  }

  @Override
  @Transactional
  public PostRelationTypeComponentAssetTypesResponse createRelationTypeComponentAssetTypesAssignments (
    UUID relationTypeComponentId,
    PostRelationTypeComponentAssetTypesRequest assignmentsRequest,
    User user
  ) throws IllegalArgumentException, AssetTypeNotFoundException, RelationTypeComponentNotFoundException {
    RelationTypeComponent relationTypeComponent = relationTypeComponentsDAO.findRelationTypeComponentById(relationTypeComponentId, false);

    List<PostRelationTypeComponentAssetTypeResponse> assetTypeResponses = assignmentsRequest.getAllowed_asset_type()
      .stream()
      .map(assignment -> {
        AssetType assetType = assetTypesDAO.findAssetTypeById(UUID.fromString(assignment.getAsset_type_id()));

        RelationTypeComponentAssetTypeAssignment parentAssetTypeAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(
          relationTypeComponent,
          assetType,
          false,
          null,
          user
        ));

        List<AssetType> childAssetTypes = assetTypesDAO.findAllAssetTypesByParentAssetTypeId(assetType.getAssetTypeId());
        childAssetTypes.forEach(childAssetType -> {
          relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(
            relationTypeComponent,
            childAssetType,
            true,
            assetType,
            user
          ));
        });

        return new PostRelationTypeComponentAssetTypeResponse(
          parentAssetTypeAssignment.getRelationTypeComponentAssetTypeAssignmentId(),
          relationTypeComponent.getRelationTypeComponentId(),
          assetType.getAssetTypeId(),
          new Timestamp(System.currentTimeMillis()),
          user.getUserId()
        );
      }).toList();

    return new PostRelationTypeComponentAssetTypesResponse(assetTypeResponses);
  }

  @Override
  public GetRelationTypeComponentAssetTypeAssignmentsResponse getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentId (
    UUID relationTypeComponentId
  ) throws RelationTypeComponentNotFoundException {
    RelationTypeComponent relationTypeComponent = relationTypeComponentsDAO.findRelationTypeComponentById(relationTypeComponentId, false);

    List<RelationTypeComponentAssetTypeAssignmentWithConnectedValues> assignments = relationTypeComponentAssetTypeAssignmentRepository.findAllWithJoinedTablesByRelationTypeComponentId(
      relationTypeComponentId
    );

    List<GetRelationTypeComponentAssetTypeAssignmentResponse> relationTypeComponentAssetTypeResponses = assignments.stream()
      .map(assignment -> new GetRelationTypeComponentAssetTypeAssignmentResponse(
        assignment.getRelationTypeComponentAssetTypeAssignmentId(),
        assignment.getAssetTypeId(),
        assignment.getAssetTypeName(),
        assignment.getIsInherited(),
        assignment.getParentAssetTypeId(),
        assignment.getParentAssetTypeName(),
        assignment.getCreatedOn(),
        assignment.getCreatedBy()
      )).toList();

    return new GetRelationTypeComponentAssetTypeAssignmentsResponse(
      relationTypeComponent.getRelationTypeComponentId(),
      relationTypeComponent.getRelationTypeComponentName(),
      relationTypeComponentAssetTypeResponses
    );
  }

  @Override
  @Transactional
  public void deleteRelationTypeComponentAssetTypeAssignment (
    UUID relationTypeComponentAssetTypeAssignmentId,
    User user
  ) throws
    AssetsTypeIsUsedInRelationsException,
    RelationTypeComponentAssetTypeAssignmentNotFound,
    RelationTypeComponentAssetTypeAssignmentIsInherited
  {
    RelationTypeComponentAssetTypeAssignment relationTypeComponentAssetTypeAssignment = findRelationTypeComponentAssetTypeAssignmentById(relationTypeComponentAssetTypeAssignmentId, false);

    if (relationTypeComponentAssetTypeAssignment.getIsInherited()) {
      throw new RelationTypeComponentAssetTypeAssignmentIsInherited();
    }

    Boolean isRelationsExists = relationsDAO.isRelationsExistsByAssetId(relationTypeComponentAssetTypeAssignment.getAssetType().getAssetTypeId(), relationTypeComponentAssetTypeAssignment.getRelationTypeComponent().getRelationTypeComponentId());
    if (isRelationsExists) {
      throw new AssetsTypeIsUsedInRelationsException();
    }

    List<RelationTypeComponentAssetTypeAssignment> childAssignments = relationTypeComponentAssetTypeAssignmentRepository.findAllChildAssignmentsByRelationTypeComponentAssetTypeAssignmentId(relationTypeComponentAssetTypeAssignmentId);
    childAssignments.forEach(assignment -> {
      Boolean hasRelations = relationsDAO.isRelationsExistsByAssetId(assignment.getAssetType().getAssetTypeId(), assignment.getRelationTypeComponent().getRelationTypeComponentId());
      if (hasRelations) {
        throw new AssetsTypeIsUsedInRelationsException();
      }

      assignment.setIsDeleted(true);
      assignment.setDeletedBy(user);
      assignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

      relationTypeComponentAssetTypeAssignmentRepository.save(assignment);
    });

    relationTypeComponentAssetTypeAssignment.setIsDeleted(true);
    relationTypeComponentAssetTypeAssignment.setDeletedBy(user);
    relationTypeComponentAssetTypeAssignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    relationTypeComponentAssetTypeAssignmentRepository.save(relationTypeComponentAssetTypeAssignment);
  }
}
