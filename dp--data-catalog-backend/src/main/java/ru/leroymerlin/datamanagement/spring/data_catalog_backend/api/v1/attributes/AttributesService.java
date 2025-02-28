package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeTypeNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.AttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.get.GetAttributesResponse;

public interface AttributesService {
  PostAttributeResponse createAttribute (
    PostAttributeRequest attributeRequest,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    AttributeTypeNotAllowedException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException;

  PatchAttributeResponse updateAttribute (
    UUID attributeId,
    PatchAttributeRequest attributeRequest,
    User user
  ) throws
    AttributeNotFoundException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException;

  AttributeResponse getAttributeById (UUID attributeId) throws AttributeNotFoundException;

  GetAttributesResponse getAttributesByParams (UUID assetId, UUID attributeTypeId, Integer pageNumber, Integer pageSize);

  void deleteAttributeById (UUID attributeId, User user) throws AttributeNotFoundException;

  List<PostAttributeResponse> createAttributesBulk (
    List<PostAttributeRequest> attributeRequests,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    AttributeTypeNotAllowedException,
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    SomeRequiredFieldsAreEmptyException,
    AttributeValueMaskValidationException;

  void deleteAttributesBulk (List<UUID> attributeRequests, User user) throws AttributeNotFoundException;

  List<PatchAttributeResponse> updateAttributesBulk (List<PatchBulkAttributeRequest> attributesRequest, User user);
}
