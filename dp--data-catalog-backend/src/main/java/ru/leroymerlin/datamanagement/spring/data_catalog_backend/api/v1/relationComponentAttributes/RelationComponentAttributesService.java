package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeTypeNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.AttributeTypeNotAllowedForRelationComponentException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.RelationComponentAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationComponentNotFoundException;

/**
 * @author juliwolf
 */

public interface RelationComponentAttributesService {
  PostRelationComponentAttributeResponse createRelationComponentAttribute (
    PostRelationComponentAttributeRequest request,
    User user
  ) throws
    AttributeTypeNotFoundException,
    AttributeTypeNotAllowedException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    RelationComponentNotFoundException,
    AttributeValueMaskValidationException,
    AttributeTypeNotAllowedForRelationComponentException;

  PatchRelationComponentAttributeResponse updateRelationComponentAttribute (
    UUID relationComponentAttributeId,
    PatchRelationComponentAttributeRequest request,
    User user
  ) throws
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException,
    RelationComponentAttributeNotFoundException;

  GetRelationComponentAttributeResponse getRelationComponentAttributeById (UUID relationComponentAttributeId) throws RelationComponentAttributeNotFoundException;

  GetRelationComponentAttributesResponse getRelationComponentAttributesByParams (List<UUID> attributeTypeIds, List<UUID> relationComponentIds, Integer pageNumber, Integer pageSize);

  void deleteRelationComponentAttributeById (UUID relationComponentAttributeId, User user) throws RelationComponentAttributeNotFoundException;
}
