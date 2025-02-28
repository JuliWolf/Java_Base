package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.AttributeTypesAllowedValuesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.AttributeTypeAllowedValueRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeTypeAllowedValue;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeAllowedValueNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeDoesNotUseValueListException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class AttributeTypesAllowedValuesServiceTest extends ServiceWithUserIntegrationTest {

  @Autowired
  private AttributeTypeAllowedValueRepository attributeTypeAllowedValueRepository;

  @Autowired
  private AttributeTypesAllowedValuesService attributeTypesAllowedValuesService;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AttributeRepository attributeRepository;
  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;

  private Asset asset;
  private AssetType assetType;
  private AttributeType attributeType;

  @BeforeAll
  public void prepareData () {
    assetType = assetTypeRepository.save(new AssetType("asset type name", "asset description", "ad", "red", language, user));
    asset = assetRepository.save(new Asset("some asset", assetType, "asset desc", language, null, null, user));
  }

  @AfterAll
  public void clearData () {
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearTables() {
    attributeTypeAllowedValueRepository.deleteAll();
    attributeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  @Test
  public void createAttributeTypeAllowedValueAttributeTypeNotFoundIntegrationTest () {
    try {
      assertThrows(AttributeTypeNotFoundException.class, () -> attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(new PostAllowedValueRequest(UUID.randomUUID().toString(), "some value"), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAttributeTypeAllowedValueAttributeTypeDoesNotUseValueListIntegrationTest () {
    try {
      AttributeType attributeType = attributeTypeRepository.save(new AttributeType("test name","test description", AttributeKindType.DECIMAL, null,null, language, user));

      assertThrows(AttributeTypeDoesNotUseValueListException.class, () -> attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(new PostAllowedValueRequest(attributeType.getAttributeTypeId().toString(), "some value"), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAttributeTypeAllowedValueSuccessIntegrationTest () {
    try {
      AttributeType multipleAttributeType = attributeTypeRepository.save(new AttributeType("multiple name","test description", AttributeKindType.MULTIPLE_VALUE_LIST, null,null, language, user));
      PostAllowedValueResponse attributeTypeAllowedValue = attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(new PostAllowedValueRequest(multipleAttributeType.getAttributeTypeId().toString(), "some value"), user);

      assertAll(
        () -> assertEquals(multipleAttributeType.getAttributeTypeId(), attributeTypeAllowedValue.getAttribute_type_id()),
        () -> assertEquals(user.getUserId(), attributeTypeAllowedValue.getCreated_by())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAttributeTypeAllowedValueByIdAttributeTypeNotFoundIntegrationTest () {
    try {
      assertThrows(AttributeTypeAllowedValueNotFoundException.class, () -> attributeTypesAllowedValuesService.deleteAttributeTypeAllowedValueById(UUID.randomUUID(), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  // Does not work due to h2 not support string_to_array
//  @Test
//  public void deleteAttributeTypeAllowedValueByIdValueIdUsedInAttributeIntegrationTest () {
//    try {
//      String value = "some;value;test";
//
//      AttributeType multipleAttributeType = attributeTypeRepository.save(new AttributeType("multiple name","test description", AttributeKindType.MULTIPLE_VALUE_LIST, null,null, language, user));
//
//      AttributeTypeAllowedValue allowedValue = attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(multipleAttributeType, "value", language, user));
//
//      Attribute attribute = new Attribute(multipleAttributeType, asset, language, user);
//      attribute.setValue(value);
//      attributeRepository.save(attribute);
//
//      assertThrows(AllowedValueIsUsedInAttributeException.class, () -> attributeTypesAllowedValuesService.deleteAttributeTypeAllowedValueById(allowedValue.getValueId(), user));
//    } catch (Exception exception) {
//      throw new RuntimeException(exception);
//    }
//  }

  @Test
  public void deleteAttributeTypeAllowedValueByIdValueAlreadyDeletedIntegrationTest () {
    try {
      AttributeType multipleAttributeType = attributeTypeRepository.save(new AttributeType("multiple name","test description", AttributeKindType.MULTIPLE_VALUE_LIST, null,null, language, user));

      AttributeTypeAllowedValue allowedValue = new AttributeTypeAllowedValue(multipleAttributeType, "value", language, user);
      allowedValue.setIsDeleted(true);
      attributeTypeAllowedValueRepository.save(allowedValue);

      assertThrows(AttributeTypeAllowedValueNotFoundException.class, () -> attributeTypesAllowedValuesService.deleteAttributeTypeAllowedValueById(allowedValue.getValueId(), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  // Does not work due to h2 not support string_to_array
//  @Test
//  public void deleteAttributeTypeAllowedValueByIdSuccessIntegrationTest () {
//    try {
//      AttributeType multipleAttributeType = attributeTypeRepository.save(new AttributeType("multiple name","test description", AttributeKindType.MULTIPLE_VALUE_LIST, null,null, language, user));
//
//      AttributeTypeAllowedValue allowedValue = attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(multipleAttributeType, "value", language, user));
//
//      attributeTypesAllowedValuesService.deleteAttributeTypeAllowedValueById(allowedValue.getValueId(), user);
//
//      Optional<AttributeTypeAllowedValue> valueRepositoryById = attributeTypeAllowedValueRepository.findById(allowedValue.getValueId());
//
//      assertAll(
//        () -> assertTrue(valueRepositoryById.get().getIsDeleted()),
//        () -> assertNotNull(valueRepositoryById.get().getDeletedBy())
//      );
//    } catch (Exception exception) {
//      throw new RuntimeException(exception);
//    }
//  }
}
