package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.IncorrectRoleForResponsibilityInheritanceException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.IncorrectRoleInHierarchyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get.GetRelationTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PatchRelationTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PostRelationTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PostRelationTypeResponse;

public interface RelationTypesService {
  PostRelationTypeResponse createRelationType (
    PostRelationTypeRequest relationTypeRequest,
    User user
  ) throws
    IncorrectRoleInHierarchyException,
    IncorrectRoleForResponsibilityInheritanceException;

  RelationTypeResponse getRelationTypeById (UUID relationTypeId) throws RelationTypeNotFoundException;

  GetRelationTypesResponse getRelationTypesByParams (
    String relationTypeName,
    Integer componentNumber,
    Boolean hierarchyFlag,
    Boolean responsibilityInheritanceFlag,
    UUID allowedAssetTypeId,
    Boolean selfRelatedFlag,
    Boolean uniquenessFlag,
    Integer pageNumber,
    Integer pageSize
  );

  void deleteRelationTypeById (UUID relationTypeId, User user) throws RelationTypeNotFoundException;

  RelationTypeResponse updateRelationType (
    UUID relationTypeID,
    PatchRelationTypeRequest relationTypeRequest,
    User user
  ) throws
    RelationTypeNotFoundException,
    IncorrectRoleInHierarchyException,
    RelationTypeComponentNotFoundException,
    IncorrectRoleForResponsibilityInheritanceException;

  void createResponsibilities(List<RelationComponent> relationComponents, User user);
}
