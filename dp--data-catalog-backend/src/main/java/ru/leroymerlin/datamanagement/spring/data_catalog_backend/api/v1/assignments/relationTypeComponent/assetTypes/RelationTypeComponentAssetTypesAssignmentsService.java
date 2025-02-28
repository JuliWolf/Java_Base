package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentIsInherited;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.get.GetRelationTypeComponentAssetTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

/**
 * @author JuliWolf
 */

public interface RelationTypeComponentAssetTypesAssignmentsService {
  PostRelationTypeComponentAssetTypesResponse createRelationTypeComponentAssetTypesAssignments (
    UUID relationTypeComponentId,
    PostRelationTypeComponentAssetTypesRequest assignmentsRequest,
    User user
  ) throws IllegalArgumentException, AssetTypeNotFoundException, RelationTypeComponentNotFoundException;

  GetRelationTypeComponentAssetTypeAssignmentsResponse getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentId (
    UUID relationTypeComponentId
  ) throws RelationTypeComponentNotFoundException;

  void deleteRelationTypeComponentAssetTypeAssignment (
    UUID relationTypeComponentAssetTypeAssignmentId,
    User user
  ) throws RelationTypeComponentAssetTypeAssignmentNotFound, RelationTypeComponentAssetTypeAssignmentIsInherited;
}
