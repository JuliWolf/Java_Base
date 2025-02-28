package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AllowedValueIsUsedInAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeAllowedValueNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeDoesNotUseValueListException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

public interface AttributeTypesAllowedValuesService {
  void deleteAttributeTypeAllowedValueById (
    UUID attributeTypeAllowedValueId,
    User user
  ) throws AttributeTypeAllowedValueNotFoundException, AllowedValueIsUsedInAttributeException;

  PostAllowedValueResponse createAttributeTypeAllowedValue (
    PostAllowedValueRequest request,
    User user
  ) throws AttributeTypeNotFoundException, AttributeTypeDoesNotUseValueListException;
}
