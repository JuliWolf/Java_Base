package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues;

import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.AttributeTypeAllowedValueRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeTypeAllowedValue;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesDAO;

@Service
public class AttributeTypesAllowedValuesServiceImpl extends AttributeTypesAllowedValuesDAO implements AttributeTypesAllowedValuesService {
  @Autowired
  private LanguageService languageService;

  @Autowired
  private AttributesDAO attributesDAO;

  @Autowired
  private RelationAttributesDAO relationAttributesDAO;

  @Autowired
  private RelationComponentAttributesDAO relationComponentAttributesDAO;

  @Autowired
  private AttributeTypesDAO attributeTypesDAO;

  @Autowired
  private AttributeTypeAllowedValueRepository attributeTypeAllowedValueRepository;

  @Override
  public PostAllowedValueResponse createAttributeTypeAllowedValue (
    PostAllowedValueRequest request,
    User user
  ) throws AttributeTypeNotFoundException, AttributeTypeDoesNotUseValueListException {
    AttributeType attributeType = attributeTypesDAO.findAttributeTypeById(UUID.fromString(request.getAttribute_type_id()), false);

    if (
      !attributeType.getAttributeKindType().equals(AttributeKindType.SINGLE_VALUE_LIST) &&
      !attributeType.getAttributeKindType().equals(AttributeKindType.MULTIPLE_VALUE_LIST)
    ) {
      throw new AttributeTypeDoesNotUseValueListException();
    }

    Language ru = languageService.getLanguage("ru");
    AttributeTypeAllowedValue allowedValue = attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(
      attributeType,
      request.getValue(),
      ru,
      user
    ));

    return new PostAllowedValueResponse(
      allowedValue.getValueId(),
      allowedValue.getAttributeType().getAttributeTypeId(),
      allowedValue.getValue(),
      ru.getLanguage(),
      allowedValue.getCreatedOn(),
      user.getUserId()
    );
  }

  @Override
  public void deleteAttributeTypeAllowedValueById (
    UUID attributeTypeAllowedValueId,
    User user
  ) throws AttributeTypeAllowedValueNotFoundException, AllowedValueIsUsedInAttributeException {
    AttributeTypeAllowedValue attributeTypeAllowedValue = findAttributeTypeAllowedValueById(attributeTypeAllowedValueId);

    UUID attributeWithValue = attributesDAO.findFirstAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue(
      attributeTypeAllowedValue.getAttributeType().getAttributeTypeId(),
      attributeTypeAllowedValue.getValue()
    );
    if (attributeWithValue != null) {
      throw new AllowedValueIsUsedInAttributeException(attributeWithValue);
    }

    UUID relationAttributeWithValue = relationAttributesDAO.findFirstRelationAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue(
      attributeTypeAllowedValue.getAttributeType().getAttributeTypeId(),
      attributeTypeAllowedValue.getValue()
    );
    if (relationAttributeWithValue != null) {
      throw new AllowedValueIsUsedInRelationAttributeException(relationAttributeWithValue);
    }

    UUID relationComponentAttributeWithValue = relationComponentAttributesDAO.findFirstRelationComponentAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue(
      attributeTypeAllowedValue.getAttributeType().getAttributeTypeId(),
      attributeTypeAllowedValue.getValue()
    );
    if (relationComponentAttributeWithValue != null) {
      throw new AllowedValueIsUsedInRelationComponentAttributeException(relationComponentAttributeWithValue);
    }

    attributeTypeAllowedValue.setIsDeleted(true);
    attributeTypeAllowedValue.setDeletedBy(user);
    attributeTypeAllowedValue.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    attributeTypeAllowedValueRepository.save(attributeTypeAllowedValue);
  }
}
