package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.RelationAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationNotFoundException;

/**
 * @author juliwolf
 */

public interface RelationAttributesService {
  PostRelationAttributeResponse createRelationAttribute (PostRelationAttributeRequest request, User user) throws RelationNotFoundException, AttributeTypeNotFoundException;

  PatchRelationAttributeResponse updateRelationAttribute (
    UUID relationAttributeId,
    PatchRelationAttributeRequest patchRelationAttributeRequest,
    User user
  ) throws RelationAttributeNotFoundException;

  GetRelationAttributeResponse getRelationAttributeById (UUID relationAttributeId) throws RelationAttributeNotFoundException;

  GetRelationAttributesResponse getRelationAttributesByParams (UUID relationId, List<UUID> attributeTypeIds, Integer pageNumber, Integer pageSize);

  void deleteRelationAttributeById (UUID relationAttributeId, User user) throws RelationAttributeNotFoundException;
}
