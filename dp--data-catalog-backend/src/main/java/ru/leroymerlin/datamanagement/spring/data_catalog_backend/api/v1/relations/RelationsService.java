package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationsAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsResponse;

/**
 * @author juliwolf
 */

public interface RelationsService {
  PostRelationsResponse createRelations (
    PostRelationsRequest relationsRequest,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    RelationAlreadyExistsException,
    InvalidAssetTypeForComponentException,
    RelationTypeNotFoundException,
    InvalidNumberOfComponentsException,
    InvalidComponentForRelationTypeException,
    InvalidHierarchyBetweenAssetsException,
    RelationTypeComponentNotFoundException,
    RelationTypeDoesNotAllowedRelatedAssetException;

  GetRelationResponse getRelationById (UUID relationId) throws RelationNotFoundException;

  GetRelationsResponse getRelationsByParams (UUID assetId, UUID relationTypeId, UUID relationTypeComponentId, Boolean hierarchyFlag, Boolean responsibilityInheritanceFlag, Integer pageNumber, Integer pageSize);

  void deleteRelation (UUID relationId, User user) throws RelationNotFoundException;

  GetRelationsAttributesResponse getRelationAttributes (UUID relationId) throws RelationNotFoundException;

  List<PostRelationsResponse> createRelationsBulk (
    List<PostRelationsRequest> relationsRequest,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    RelationAlreadyExistsException,
    InvalidAssetTypeForComponentException,
    RelationTypeNotFoundException,
    InvalidNumberOfComponentsException,
    InvalidComponentForRelationTypeException,
    InvalidHierarchyBetweenAssetsException,
    RelationTypeComponentNotFoundException,
    RelationTypeDoesNotAllowedRelatedAssetException;

  void deleteRelationsBulk (List<UUID> relationIds, User user) throws RelationNotFoundException;
}
