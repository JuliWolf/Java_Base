package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.AttributeValueValidator;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.linkUsage.LinkUsageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;

/**
 * @author juliwolf
 */

@Service
public class BulkAttributesService {
  private final LanguageService languageService;

  private final AttributeValueValidator attributeValueValidator;

  private final LinkUsageService linkUsageService;

  private final AttributesDAO attributesDAO;

  public BulkAttributesService (
    LanguageService languageService,
    AttributeValueValidator attributeValueValidator,
    LinkUsageService linkUsageService,
    AttributesDAO attributesDAO
  ) {
    this.languageService = languageService;
    this.attributeValueValidator = attributeValueValidator;
    this.linkUsageService = linkUsageService;

    this.attributesDAO = attributesDAO;
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public List<PostAttributeResponse> createAttributes (
    List<PostAttributeRequest> attributeRequests,
    Map<UUID, Asset> assetsMap,
    Map<UUID, AttributeType> attributeTypesMap,
    User user
  ) throws
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException
  {
    Language ru = languageService.getLanguage("ru");

    return attributeRequests.stream()
      .map(request -> {
        UUID assetId = request.getAsset_id();
        UUID attributeTypeId = UUID.fromString(request.getAttribute_type_id());

        Asset asset = assetsMap.get(assetId);
        AttributeType attributeType = attributeTypesMap.get(attributeTypeId);

        attributeValueValidator.validateValueType(
          request.getValue(),
          attributeType.getAttributeTypeId(),
          attributeType.getValidationMask(),
          attributeType.getAttributeKindType(),
          ObjectUtils.convertObjectToMap(request)
        );

        Attribute attribute = new Attribute(attributeType, asset, ru, user);
        attributeValueValidator.setAttributeValueByType(attribute, request.getValue(), attributeType.getAttributeKindType());
        attribute = attributesDAO.attributeRepository.save(attribute);

        attributesDAO.createAttributeHistory(attribute, MethodType.POST);

        if (attributeType.getAttributeKindType().equals(AttributeKindType.RTF)) {
          linkUsageService.parseAttributeValueToAttributeLink(attribute, user);
          linkUsageService.parseAttributeValueToImageLink(attribute, user);
        }

        return new PostAttributeResponse(
          attribute.getAttributeId(),
          attributeType.getAttributeTypeId(),
          asset.getAssetId(),
          attribute.getValue(),
          attribute.getIsInteger(),
          attribute.getValueNumeric(),
          attribute.getValueBoolean(),
          attribute.getValueDatetime(),
          ru.getLanguage(),
          attribute.getCreatedOn(),
          user.getUserId()
        );
      }).toList();
  }
}
