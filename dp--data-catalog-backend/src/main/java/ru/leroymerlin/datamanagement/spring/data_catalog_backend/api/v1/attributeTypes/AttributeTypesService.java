package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeValueAlreadyAssignedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.AttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PatchAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeResponse;

/**
 * @author JuliWolf
 */
public interface AttributeTypesService {

  PostAttributeTypeResponse createAttributeType (
    PostAttributeTypeRequest attributeTypeRequest, User user
  ) throws AttributeTypeValueAlreadyAssignedException;

  AttributeTypeResponse updateAttributeType (
    UUID attributeTypeId, PatchAttributeTypeRequest attributeTypeRequest, User user
  ) throws
    AttributeTypeNotFoundException,
    IncompatibleAttributeKindException,
    ValidationMaskCantBeAppliedException,
    AttributeDoesNotMatchTheMaskException;

  GetAttributeTypeResponse getAttributeTypeById (UUID attributeTypeId) throws AttributeTypeNotFoundException;

  GetAttributeTypesResponse getAttributeTypeByParams (
    String attributeTypeName,
    String attributeTypeDescription,
    AttributeKindType attributeKind,
    Integer pageNumber,
    Integer pageSize
  );

  void deleteAttributeTypeById (
    UUID attributeTypeId, User user
  ) throws AttributeTypeNotFoundException, AttributeWithAttributeTypeExistsException;
}
