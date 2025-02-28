package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.AttributeTypeAllowedValueRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetLinkUsage.AssetLinkUsageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeHistory.AttributeHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.imageLinkUsage.ImageLinkUsageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AssetAlreadyHasAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeTypeNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.AttributeResponse;

import static org.junit.jupiter.api.Assertions.*;

public class AttributesServiceTest extends ServiceWithUserIntegrationTest {

  @Autowired
  private AttributeRepository attributeRepository;

  @Autowired
  private AttributesService attributesService;

  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AttributeHistoryRepository attributeHistoryRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  @Autowired
  private AttributeTypeAllowedValueRepository attributeTypeAllowedValueRepository;
  @Autowired
  private AssetLinkUsageRepository assetLinkUsageRepository;
  @Autowired
  private ImageLinkUsageRepository imageLinkUsageRepository;

  AssetType simpleAssetType;
  AssetType differentAssetType;
  Asset simpleAsset;
  Asset differentAsset;
  AttributeType booleanAttributeType;
  AttributeType textAttributeType;
  AttributeType rtfAttributeType;
  AttributeType decimalAttributeType;
  AttributeType dateAttributeType;
  AttributeType multipleListAttributeType;
  AttributeType withNoAssignmentsAttributeType;
  AssetTypeAttributeTypeAssignment simpleAssetBooleanAttributeTypeAssignment;
  AssetTypeAttributeTypeAssignment simpleAssetDecimalAttributeTypeAssignment;
  AssetTypeAttributeTypeAssignment differentAssetDateAttributeTypeAssignment;
  AssetTypeAttributeTypeAssignment differentAssetTextAttributeTypeAssignment;
  AssetTypeAttributeTypeAssignment differentAssetERFAttributeTypeAssignment;
  AssetTypeAttributeTypeAssignment differentAssetMultipleListAttributeTypeAssignment;
  AttributeTypeAllowedValue allowedValueWithValue1;
  AttributeTypeAllowedValue allowedValueWithValue2;

  @BeforeAll
  public void prepareData () {
    simpleAssetType = assetTypeRepository.save(new AssetType("simple asset type", "some simple description", "sa", "blue", language, user));
    differentAssetType = assetTypeRepository.save(new AssetType("different asset type", "different desc", "da", "purple", language, user));

    simpleAsset = assetRepository.save(new Asset("simple asset", simpleAssetType, "simple", language, null, null, user));
    differentAsset = assetRepository.save(new Asset("different asset", differentAssetType, "different", language, null, null, user));

    booleanAttributeType = attributeTypeRepository.save(new AttributeType("boolean attribute type", "attribute with boolean type", AttributeKindType.BOOLEAN, null, null, language, user));
    textAttributeType = attributeTypeRepository.save(new AttributeType("text attribute type", "attribute with text type", AttributeKindType.TEXT, null, null, language, user));
    rtfAttributeType = attributeTypeRepository.save(new AttributeType("rtf attribute type", "attribute with rtf type", AttributeKindType.RTF, null, null, language, user));
    decimalAttributeType = attributeTypeRepository.save(new AttributeType("decimal attribute type", "attribute with decimal type", AttributeKindType.DECIMAL, null, null, language, user));
    dateAttributeType = attributeTypeRepository.save(new AttributeType("date attribute type", "attribute with date type", AttributeKindType.DATE, null, null,language, user));
    multipleListAttributeType = attributeTypeRepository.save(new AttributeType("multiple list attribute type", "attribute with multiple list type", AttributeKindType.MULTIPLE_VALUE_LIST, null, null,language, user));
    withNoAssignmentsAttributeType = attributeTypeRepository.save(new AttributeType("lonely attribute type", "attribute with empty assignments", AttributeKindType.SINGLE_VALUE_LIST, null, null,language, user));

    simpleAssetBooleanAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, booleanAttributeType, user));
    simpleAssetDecimalAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, decimalAttributeType, user));
    differentAssetDateAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(differentAssetType, dateAttributeType, user));
    differentAssetTextAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(differentAssetType, rtfAttributeType, user));
    differentAssetERFAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(differentAssetType, textAttributeType, user));
    differentAssetMultipleListAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(differentAssetType, multipleListAttributeType, user));

    allowedValueWithValue1 = attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(multipleListAttributeType, "1", language, user));
    allowedValueWithValue2 = attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(multipleListAttributeType, "2", language, user));
  }

  @AfterAll
  public void clearData () {

    attributeTypeAllowedValueRepository.deleteAll();
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAttributes () {
    attributeHistoryRepository.deleteAll();

    textAttributeType.setValidationMask(null);
    attributeTypeRepository.save(textAttributeType);

    attributeRepository.deleteAll();
    assetLinkUsageRepository.deleteAll();
    imageLinkUsageRepository.deleteAll();
  }

  @Test
  public void createAttributeSuccessIntegrationTest () {
    try {
      PostAttributeRequest request = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");

      PostAttributeResponse response = attributesService.createAttribute(request, user);

      assertAll(
        () -> assertEquals(response.getAttribute_type_id(), booleanAttributeType.getAttributeTypeId()),
        () -> assertTrue(response.getValue_bool()),
        () -> assertNull(response.getValue_datetime())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAttributeAttributeHistoryIntegrationTest () {
    PostAttributeRequest request = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");

    PostAttributeResponse response = attributesService.createAttribute(request, user);

    List<AttributeHistory> attributeHistories = attributeHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(1, attributeHistories.size(), "attribute history size"),
      () -> assertEquals("3000-01-01 00:00:00.0", attributeHistories.get(0).getValidTo().toString(), "attribute history valid to"),
      () -> assertEquals(response.getCreated_on(), attributeHistories.get(0).getValidFrom(), "attribute history valid from")
    );
  }

  @Test
  public void createAttributeEmptyAssignmentsIntegrationTest () {
    PostAttributeRequest request = new PostAttributeRequest(withNoAssignmentsAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");

    assertThrows(AttributeTypeNotAllowedException.class, () -> attributesService.createAttribute(request, user));
  }

  @Test
  public void createAttributeValidateBooleanValueIntegrationTest () {
    assertAll(
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.createAttribute(new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "some value"), user)),
      () -> assertDoesNotThrow(() -> attributesService.createAttribute(new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true"), user))
    );
  }

  @Test
  public void createAttributeValidateTextValueIntegrationTest () {
    assertDoesNotThrow(() -> attributesService.createAttribute(new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "some value"), user));

    attributeRepository.deleteAll();
    textAttributeType.setValidationMask("^start.*");
    attributeTypeRepository.save(textAttributeType);
    assertDoesNotThrow(() -> attributesService.createAttribute(new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "start value"), user));

    attributeRepository.deleteAll();
    assertThrows(AttributeValueMaskValidationException.class, () -> attributesService.createAttribute(new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "value value"), user));
  }

  @Test
  public void createAttributeValidateDecimalValueIntegrationTest () {
    assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.createAttribute(new PostAttributeRequest(decimalAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "value value"), user));

    assertDoesNotThrow(() -> attributesService.createAttribute(new PostAttributeRequest(decimalAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "123"), user));

    attributeRepository.deleteAll();
    assertDoesNotThrow(() -> attributesService.createAttribute(new PostAttributeRequest(decimalAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "123.5"), user));

    attributeRepository.deleteAll();
    assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.createAttribute(new PostAttributeRequest(decimalAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "123."), user));
  }

  @Test
  public void createAttributeValidateDateValueIntegrationTest () {
    assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.createAttribute(new PostAttributeRequest(dateAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "value value"), user));

    assertDoesNotThrow(() -> attributesService.createAttribute(new PostAttributeRequest(dateAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), String.valueOf(System.currentTimeMillis())), user));
  }

  @Test
  public void createAttributeValidateMultipleListValueIntegrationTest () {
    assertThrows(AttributeValueNotAllowedException.class, () -> attributesService.createAttribute(new PostAttributeRequest(multipleListAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "value value"), user));

    assertDoesNotThrow(() -> attributesService.createAttribute(new PostAttributeRequest(multipleListAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "1;2;"), user));
    assertThrows(DataIntegrityViolationException.class, () -> attributesService.createAttribute(new PostAttributeRequest(multipleListAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "2"), user));
  }

  @Test
  public void createAttributeCreateAssetLinkUsageIntegrationTest () {
    attributesService.createAttribute(
      new PostAttributeRequest(
        rtfAttributeType.getAttributeTypeId().toString(),
        differentAsset.getAssetId(),
        new StringBuilder()
          .append("<p><a href=\"")
          .append(simpleAsset.getAssetId())
          .append("\">simple asset</a><p>Some text</p>")
          .append("<a class=\"link\"href=\"https://dg-data-catalog-frontend-test-shared-stage.apps.lmru.tech/asset/")
          .append(differentAsset.getAssetId())
          .append("\"> different asset</a>")
          .append("<a href=\"https://dg-data-catalog-frontend-test-shared-stage.apps.lmru.tech/assetType/")
          .append(UUID.randomUUID())
          .append("\">Some random UUID</a>")
          .toString()
      ), user
    );

    assertAll(
      () -> assertEquals(2, assetLinkUsageRepository.findAll().size()),
      () -> assertEquals(simpleAsset.getAssetId(), assetLinkUsageRepository.findAll().stream().filter(link -> link.getAsset().getAssetId().equals(simpleAsset.getAssetId())).findFirst().get().getAsset().getAssetId()),
      () -> assertEquals(differentAsset.getAssetId(), assetLinkUsageRepository.findAll().stream().filter(link -> link.getAsset().getAssetId().equals(differentAsset.getAssetId())).findFirst().get().getAsset().getAssetId())
    );
  }

  @Test
  public void createAttributesBulkSomeRequiredFieldsAreEmptyIntegrationTest () {
    PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");
    PostAttributeRequest secondRequest = new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), null, "some value");
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> attributesService.createAttributesBulk(requests, user));
  }

  @Test
  public void createAttributesBulkValueValidationExceptionIntegrationTest () {
    PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "some value");
    PostAttributeRequest secondRequest = new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "some value");
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.createAttributesBulk(requests, user));
  }

  @Test
  public void createAttributesBulkAttributeAlreadyExistsIntegrationTest () {
    attributesService.createAttribute(new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true"), user);

    PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");
    PostAttributeRequest secondRequest = new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "some value");
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(AssetAlreadyHasAttributeException.class, () -> attributesService.createAttributesBulk(requests, user));
  }

  @Test
  public void createAttributesBulkAttributeTypeNotAllowedExceptionIntegrationTest () {
    PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");
    PostAttributeRequest secondRequest = new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "some value");
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(AttributeTypeNotAllowedException.class, () -> attributesService.createAttributesBulk(requests, user));
  }

  @Test
  public void createAttributesBulkSuccessIntegrationTest () {
    PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");
    PostAttributeRequest secondRequest = new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "some value");
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertAll(
      () -> assertDoesNotThrow(() -> attributesService.createAttributesBulk(requests, user)),
      () -> assertEquals(2, attributeRepository.findAll().size())
    );
  }

  @Test
  public void createAttributesBulkAttributeHistoryIntegrationTest () {
    PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");
    PostAttributeRequest secondRequest = new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "some value");
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    List<PostAttributeResponse> postResponse = attributesService.createAttributesBulk(requests, user);

    List<AttributeHistory> attributeHistories = attributeHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, attributeHistories.size(), "attribute history size"),
      () -> assertEquals("3000-01-01 00:00:00.0", attributeHistories.get(0).getValidTo().toString(), "0 attribute history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", attributeHistories.get(1).getValidTo().toString(), "1 attribute history valid to"),
      () -> assertEquals(postResponse.get(0).getCreated_on(), attributeHistories.stream().filter(attribute -> attribute.getAssetId().equals(postResponse.get(0).getAsset_id())).findFirst().get().getValidFrom(), "0 attribute history valid from"),
      () -> assertEquals(postResponse.get(1).getCreated_on(), attributeHistories.stream().filter(attribute -> attribute.getAssetId().equals(postResponse.get(1).getAsset_id())).findFirst().get().getValidFrom(), "1 attribute history valid from")
    );
  }

  @Test
  public void createAttributesBulkCreateAssetLinkUsageIntegrationTest () {
    PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");
    PostAttributeRequest secondRequest = new PostAttributeRequest(
      rtfAttributeType.getAttributeTypeId().toString(),
      differentAsset.getAssetId(),
      new StringBuilder()
        .append("<p><a href=\"")
        .append(simpleAsset.getAssetId())
        .append("\">simple asset</a><p>Some text</p>")
        .append("<a class=\"link\"href=\"https://dg-data-catalog-frontend-test-shared-stage.apps.lmru.tech/asset/")
        .append(differentAsset.getAssetId())
        .append("\"> different asset</a>")
        .append("<a href=\"https://dg-data-catalog-frontend-test-shared-stage.apps.lmru.tech/assetType/")
        .append(UUID.randomUUID())
        .append("\">Some random UUID</a>")
        .toString()
    );
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    attributesService.createAttributesBulk(requests, user);

    assertAll(
      () -> assertEquals(2, assetLinkUsageRepository.findAll().size()),
      () -> assertEquals(simpleAsset.getAssetId(), assetLinkUsageRepository.findAll().stream().filter(link -> link.getAsset().getAssetId().equals(simpleAsset.getAssetId())).findFirst().get().getAsset().getAssetId()),
      () -> assertEquals(differentAsset.getAssetId(), assetLinkUsageRepository.findAll().stream().filter(link -> link.getAsset().getAssetId().equals(differentAsset.getAssetId())).findFirst().get().getAsset().getAssetId())
    );
  }

  @Test
  public void updateAttributeSuccessIntegrationTest () {
    try {
      Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
      multipleTypeAsset.setValue("2");
      attributeRepository.save(multipleTypeAsset);

      PatchAttributeResponse response = attributesService.updateAttribute(multipleTypeAsset.getAttributeId(), new PatchAttributeRequest("1;2;"), user);
      Optional<Attribute> updatedAttribute = attributeRepository.findById(multipleTypeAsset.getAttributeId());

      assertAll(
        () -> assertEquals("1;2;", response.getValue()),
        () -> assertNull(response.getValue_numeric()),
        () -> assertNull(response.getValue_bool()),
        () -> assertNotEquals(multipleTypeAsset.getValue(), response.getValue()),
        () -> assertNotEquals(multipleTypeAsset.getLastModifiedOn(), updatedAttribute.get().getLastModifiedOn()),
        () -> assertNotNull(updatedAttribute.get().getModifiedBy())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeAttributeHistoryIntegrationTest () {
    PostAttributeRequest postRequest = new PostAttributeRequest(multipleListAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "2");
    PostAttributeResponse postResponse = attributesService.createAttribute(postRequest, user);

    PatchAttributeResponse patchResponse = attributesService.updateAttribute(postResponse.getAttribute_id(), new PatchAttributeRequest("1;2;"), user);

    List<AttributeHistory> attributeHistories = attributeHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, attributeHistories.size(), "attribute history size"),
      () -> assertEquals(postResponse.getCreated_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.getValue())).findFirst().get().getValidFrom(), "post attribute history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponse.getValue())).findFirst().get().getValidFrom(), "patch attribute history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.getValue())).findFirst().get().getValidTo(), "post attribute history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", attributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponse.getValue())).findFirst().get().getValidTo().toString(), "patched attribute history valid to")
    );
  }

  @Test
  public void updateAttributeUpdateDateValueIntegrationTest () {
    try {
      Attribute dateTypeAsset = new Attribute(decimalAttributeType, differentAsset, language, user);
      dateTypeAsset.setValue("12345");
      attributeRepository.save(dateTypeAsset);

      assertAll(
        () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.updateAttribute(dateTypeAsset.getAttributeId(), new PatchAttributeRequest("1;2;"), user)),
        () -> assertDoesNotThrow(() -> attributesService.updateAttribute(dateTypeAsset.getAttributeId(), new PatchAttributeRequest("121424"), user))
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeUpdateTextValueIntegrationTest () {
    try {
      Attribute textTypeAsset = new Attribute(decimalAttributeType, simpleAsset, language, user);
      textTypeAsset.setValue("12345");
      attributeRepository.save(textTypeAsset);

      assertAll(
        () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.updateAttribute(textTypeAsset.getAttributeId(), new PatchAttributeRequest("1;2;"), user)),
        () -> assertDoesNotThrow(() -> attributesService.updateAttribute(textTypeAsset.getAttributeId(), new PatchAttributeRequest("1.4"), user))
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeUpdateDecimalValueIntegrationTest () {
    try {
      Attribute decimalTypeAsset = new Attribute(textAttributeType, differentAsset, language, user);
      decimalTypeAsset.setValue("123");
      attributeRepository.save(decimalTypeAsset);

      textAttributeType.setValidationMask("^he.*");
      attributeTypeRepository.save(textAttributeType);

      assertAll(
        () -> assertThrows(AttributeValueMaskValidationException.class, () -> attributesService.updateAttribute(decimalTypeAsset.getAttributeId(), new PatchAttributeRequest("1;2;"), user)),
        () -> assertDoesNotThrow(() -> attributesService.updateAttribute(decimalTypeAsset.getAttributeId(), new PatchAttributeRequest("hellow"), user))
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeUpdateBooleanValueIntegrationTest () {
    try {
      Attribute booleanTypeAsset = new Attribute(booleanAttributeType, simpleAsset, language, user);
      booleanTypeAsset.setValue("false");
      attributeRepository.save(booleanTypeAsset);

      assertAll(
        () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.updateAttribute(booleanTypeAsset.getAttributeId(), new PatchAttributeRequest("1;2;"), user)),
        () -> assertDoesNotThrow(() -> attributesService.updateAttribute(booleanTypeAsset.getAttributeId(), new PatchAttributeRequest("true"), user))
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeAttributeNotFoundIntegrationTest () {
    try {
      assertThrows(AttributeNotFoundException.class, () -> attributesService.updateAttribute(UUID.randomUUID(), new PatchAttributeRequest("1;2;"), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeClearExistingAssetLinksIntegrationTest () {
    Attribute rtfAttribute = new Attribute(rtfAttributeType, differentAsset, language, user);
    rtfAttribute.setValue(
      new StringBuilder()
        .append("<p>Some text</p>")
        .append("<a class=\"link\"href=\"https://dg-data-catalog-frontend-test-shared-stage.apps.lmru.tech/asset/")
        .append(differentAsset.getAssetId())
        .append("\"> different asset</a>")
        .append("<a href=\"https://dg-data-catalog-frontend-test-shared-stage.apps.lmru.tech/assetType/")
        .append(UUID.randomUUID())
        .append("\">Some random UUID</a>")
        .toString());

    attributeRepository.save(rtfAttribute);

    assetLinkUsageRepository.save(new AssetLinkUsage(rtfAttribute, simpleAsset, user));
    assetLinkUsageRepository.save(new AssetLinkUsage(rtfAttribute, differentAsset, user));

    attributesService.updateAttribute(rtfAttribute.getAttributeId(), new PatchAttributeRequest(new StringBuilder()
      .append("<p><a href=\"")
      .append(simpleAsset.getAssetId())
      .append("\"/><p>Some text</p>")
      .toString()
    ), user);

    assertAll(
      () -> assertEquals(1, assetLinkUsageRepository.findAllByParams(null, rtfAttribute.getAttributeId()).size()),
      () -> assertEquals(simpleAsset.getAssetId(), assetLinkUsageRepository.findAllByParams(null, rtfAttribute.getAttributeId()).get(0).getAsset().getAssetId())
    );
  }

  @Test
  public void updateAttributeBulkSuccessIntegrationTest () {
    try {
      Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
      multipleTypeAsset.setValue("2");
      attributeRepository.save(multipleTypeAsset);

      Attribute textTypeAsset = new Attribute(textAttributeType, simpleAsset, language, user);
      textTypeAsset.setValue("12345");
      attributeRepository.save(textTypeAsset);

      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(multipleTypeAsset.getAttributeId(), "1;2;");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(textTypeAsset.getAttributeId(), "test");
      List<PatchAttributeResponse> response = attributesService.updateAttributesBulk(List.of(firstRequest, secondRequest), user);
      Optional<Attribute> updatedAttribute = attributeRepository.findById(multipleTypeAsset.getAttributeId());
      Optional<Attribute> updatedSecondAttribute = attributeRepository.findById(textTypeAsset.getAttributeId());

      assertAll(
        () -> assertEquals("1;2;", response.get(0).getValue()),
        () -> assertNull(response.get(0).getValue_numeric()),
        () -> assertNull(response.get(0).getValue_bool()),
        () -> assertNotEquals(multipleTypeAsset.getValue(), response.get(0).getValue()),
        () -> assertNotEquals(multipleTypeAsset.getLastModifiedOn(), updatedAttribute.get().getLastModifiedOn()),
        () -> assertNotNull(updatedAttribute.get().getModifiedBy()),
        () -> assertEquals("test", response.get(1).getValue()),
        () -> assertNull(response.get(1).getValue_numeric()),
        () -> assertNull(response.get(1).getValue_bool()),
        () -> assertNotEquals(textTypeAsset.getValue(), response.get(1).getValue()),
        () -> assertNotEquals(textTypeAsset.getLastModifiedOn(), updatedSecondAttribute.get().getLastModifiedOn()),
        () -> assertNotNull(updatedSecondAttribute.get().getModifiedBy())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeBulkAttributeHistoryIntegrationTest () {
    PostAttributeRequest firstPostRequest = new PostAttributeRequest(multipleListAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "2");
    PostAttributeRequest secondPostRequest = new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "12345");
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstPostRequest);
    requests.add(secondPostRequest);

    List<PostAttributeResponse> postResponse = attributesService.createAttributesBulk(requests, user);

    PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(postResponse.stream().filter(attribute -> attribute.getValue().equals(firstPostRequest.getValue())).findFirst().get().getAttribute_id(), "1;2;");
    PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(postResponse.stream().filter(attribute -> attribute.getValue().equals(secondPostRequest.getValue())).findFirst().get().getAttribute_id(), "test");
    List<PatchAttributeResponse> patchResponses = attributesService.updateAttributesBulk(List.of(firstRequest, secondRequest), user);

    List<AttributeHistory> attributeHistories = attributeHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(4, attributeHistories.size(), "asset history size"),
      () -> assertEquals(postResponse.get(0).getCreated_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.get(0).getValue())).findFirst().get().getValidFrom(), "first post attribute history valid from"),
      () -> assertEquals(postResponse.get(1).getCreated_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.get(1).getValue())).findFirst().get().getValidFrom(), "second post attribute history valid from"),
      () -> assertEquals(patchResponses.get(0).getLast_modified_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponses.get(0).getValue())).findFirst().get().getValidFrom(), "first patch attribute history valid from"),
      () -> assertEquals(patchResponses.get(1).getLast_modified_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponses.get(1).getValue())).findFirst().get().getValidFrom(), "second patch attribute history valid from"),
      () -> assertEquals(patchResponses.get(0).getLast_modified_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.get(0).getValue())).findFirst().get().getValidTo(), "first post attribute history valid to"),
      () -> assertEquals(patchResponses.get(1).getLast_modified_on(), attributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.get(1).getValue())).findFirst().get().getValidTo(), "second post attribute history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", attributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponses.get(0).getValue())).findFirst().get().getValidTo().toString(), "first patched attribute history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", attributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponses.get(1).getValue())).findFirst().get().getValidTo().toString(), "second patched attribute history valid to")
    );
  }

  @Test
  public void updateAttributeBulkUpdateDateValueIntegrationTest () {
    try {
      Attribute dateTypeAsset = new Attribute(decimalAttributeType, differentAsset, language, user);
      dateTypeAsset.setValue("12345");
      attributeRepository.save(dateTypeAsset);

      Attribute textTypeAsset = new Attribute(textAttributeType, simpleAsset, language, user);
      textTypeAsset.setValue("12345");
      attributeRepository.save(textTypeAsset);

      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(dateTypeAsset.getAttributeId(), "1;2;");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(textTypeAsset.getAttributeId(), "test");

      assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.updateAttributesBulk(List.of(firstRequest, secondRequest), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeBulkUpdateTextValueIntegrationTest () {
    try {
      Attribute textTypeAsset = new Attribute(textAttributeType, differentAsset, language, user);
      textTypeAsset.setValue("123");
      attributeRepository.save(textTypeAsset);
      textAttributeType.setValidationMask("^he.*");
      attributeTypeRepository.save(textAttributeType);

      Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
      multipleTypeAsset.setValue("2");
      attributeRepository.save(multipleTypeAsset);

      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(multipleTypeAsset.getAttributeId(), "1;2;");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(textTypeAsset.getAttributeId(), "1.4");

      assertThrows(AttributeValueMaskValidationException.class, () -> attributesService.updateAttributesBulk(List.of(firstRequest, secondRequest), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeBulkUpdateBooleanValueIntegrationTest () {
    try {
      Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
      multipleTypeAsset.setValue("2");
      attributeRepository.save(multipleTypeAsset);

      Attribute booleanTypeAsset = new Attribute(booleanAttributeType, simpleAsset, language, user);
      booleanTypeAsset.setValue("false");
      attributeRepository.save(booleanTypeAsset);

      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(multipleTypeAsset.getAttributeId(), "1;2;");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(booleanTypeAsset.getAttributeId(), "1;2;");

     assertThrows(AttributeInvalidDataTypeException.class, () -> attributesService.updateAttributesBulk(List.of(firstRequest, secondRequest), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeBulkAttributeNotFoundIntegrationTest () {
    try {
      Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
      multipleTypeAsset.setValue("2");
      attributeRepository.save(multipleTypeAsset);

      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(UUID.randomUUID(), "1;2;");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(multipleTypeAsset.getAttributeId(), "1;2;");

      assertThrows(AttributeNotFoundException.class, () -> attributesService.updateAttributesBulk(List.of(firstRequest, secondRequest), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeBulkClearExistingAssetLinksIntegrationTest () {
    Attribute rtfAttribute = new Attribute(rtfAttributeType, differentAsset, language, user);
    rtfAttribute.setValue(
      new StringBuilder()
        .append("<p>Some text</p>")
        .append("<a class=\"link\"href=\"https://dg-data-catalog-frontend-test-shared-stage.apps.lmru.tech/asset/")
        .append(differentAsset.getAssetId())
        .append("\"> different asset</a>")
        .append("<a href=\"https://dg-data-catalog-frontend-test-shared-stage.apps.lmru.tech/assetType/")
        .append(UUID.randomUUID())
        .append("\">Some random UUID</a>")
        .toString());

    attributeRepository.save(rtfAttribute);

    assetLinkUsageRepository.save(new AssetLinkUsage(rtfAttribute, simpleAsset, user));
    assetLinkUsageRepository.save(new AssetLinkUsage(rtfAttribute, differentAsset, user));

    Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
    multipleTypeAsset.setValue("2");
    attributeRepository.save(multipleTypeAsset);

    PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(multipleTypeAsset.getAttributeId(), "1;2;");
    PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(rtfAttribute.getAttributeId(), new StringBuilder()
      .append("<p><a href=\"")
      .append(simpleAsset.getAssetId())
      .append("\"/><p>Some text</p>")
      .toString());

    List<PatchAttributeResponse> response = attributesService.updateAttributesBulk(List.of(firstRequest, secondRequest), user);

    assertAll(
      () -> assertEquals(1, assetLinkUsageRepository.findAllByParams(null, rtfAttribute.getAttributeId()).size()),
      () -> assertEquals(simpleAsset.getAssetId(), assetLinkUsageRepository.findAllByParams(null, rtfAttribute.getAttributeId()).get(0).getAsset().getAssetId())
    );
  }

  @Test
  public void getAttributeByIdSuccessIntegrationTest () {
    try {
      Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
      multipleTypeAsset.setValue("2");
      attributeRepository.save(multipleTypeAsset);

      AttributeResponse response = attributesService.getAttributeById(multipleTypeAsset.getAttributeId());

      assertAll(
        () -> assertEquals(multipleTypeAsset.getValue(), response.getValue()),
        () -> assertEquals(multipleTypeAsset.getAsset().getAssetId(), response.getAsset_id()),
        () -> assertEquals(multipleTypeAsset.getAttributeType().getAttributeTypeId(), response.getAttribute_type_id())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAttributeByIdAttributeNotFoundIntegrationTest () {
    try {
      assertThrows(AttributeNotFoundException.class, () -> attributesService.getAttributeById(UUID.randomUUID()));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAttributeByIdDeletedAttributeIntegrationTest () {
    try {
      Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
      multipleTypeAsset.setIsDeleted(true);
      attributeRepository.save(multipleTypeAsset);

      assertThrows(AttributeNotFoundException.class, () -> attributesService.getAttributeById(multipleTypeAsset.getAttributeId()));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAttributesByParamsPaginationIntegrationTest () {
    generateAttributes(130);

      assertAll(
        () -> assertEquals(50, attributesService.getAttributesByParams(null, null, 0, 50).getResults().size()),
        () -> assertEquals(100, attributesService.getAttributesByParams(null, null, 0, 130).getResults().size()),
        () -> assertEquals(2, attributesService.getAttributesByParams(null, null, 0, 2).getResults().size()),
        () -> assertEquals(0, attributesService.getAttributesByParams(null, null, 4, 100).getResults().size()),
        () -> assertEquals(130, attributesService.getAttributesByParams(null, null, 4, 100).getTotal())
      );
  }

  @Test
  public void getAttributesByParamsSuccessIntegrationTest () {
    try {
      attributeRepository.save(new Attribute(multipleListAttributeType, differentAsset, language, user));
      attributeRepository.save(new Attribute(decimalAttributeType, simpleAsset, language, user));
      attributeRepository.save(new Attribute(booleanAttributeType, simpleAsset, language, user));
      attributeRepository.save(new Attribute(dateAttributeType, differentAsset, language, user));
      attributeRepository.save(new Attribute(textAttributeType, differentAsset, language, user));

      assertAll(
        () -> assertEquals(5, attributesService.getAttributesByParams(null, null, 0, 50).getResults().size()),
        () -> assertEquals(2, attributesService.getAttributesByParams(simpleAsset.getAssetId(), null, 0, 50).getResults().size()),
        () -> assertEquals(3, attributesService.getAttributesByParams(differentAsset.getAssetId(), null, 0, 50).getResults().size()),
        () -> assertEquals(1, attributesService.getAttributesByParams(null, dateAttributeType.getAttributeTypeId(), 0, 50).getResults().size()),
        () -> assertEquals(1, attributesService.getAttributesByParams(null, multipleListAttributeType.getAttributeTypeId(), 0, 50).getResults().size()),
        () -> assertEquals(0, attributesService.getAttributesByParams(simpleAsset.getAssetId(), multipleListAttributeType.getAttributeTypeId(), 0, 50).getResults().size())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAttributeByIdSuccessIntegrationTest () {
    try {
      Attribute multipleTypeAsset = attributeRepository.save(new Attribute(multipleListAttributeType, differentAsset, language, user));

      attributesService.deleteAttributeById(multipleTypeAsset.getAttributeId(), user);

      Optional<Attribute> deletedAttribute = attributeRepository.findById(multipleTypeAsset.getAttributeId());

      assertAll(
        () -> assertNotNull(deletedAttribute.get().getDeletedOn()),
        () -> assertTrue(deletedAttribute.get().getIsDeleted())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAttributeByIdAttributeHistoryIntegrationTest () {
    PostAttributeRequest postRequest = new PostAttributeRequest(multipleListAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "2");
    PostAttributeResponse postResponse = attributesService.createAttribute(postRequest, user);

    attributesService.deleteAttributeById(postResponse.getAttribute_id(), user);

    Optional<Attribute> deletedAttribute = attributeRepository.findById(postResponse.getAttribute_id());

    List<AttributeHistory> attributeHistories = attributeHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, attributeHistories.size(), "attribute history size"),
      () -> assertEquals(postResponse.getCreated_on(), attributeHistories.stream().filter(asset -> asset.getValue().equals(postResponse.getValue())).findFirst().get().getValidFrom(), "post attribute history valid from"),
      () -> assertEquals(deletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(AttributeHistory::getIsDeleted).findFirst().get().getValidFrom(), "deleted attribute history valid from"),
      () -> assertEquals(deletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(asset -> asset.getValue().equals(postResponse.getValue())).findFirst().get().getValidTo(), "post attribute history valid to"),
      () -> assertEquals(deletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(AttributeHistory::getIsDeleted).findFirst().get().getValidTo(), "deleted attribute history valid to")
    );
  }

  @Test
  public void deleteAttributeByIdAttributeNotFoundIntegrationTest () {
    try {
      assertThrows(AttributeNotFoundException.class, () -> attributesService.deleteAttributeById(UUID.randomUUID(), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAttributeByIdAttributeAlreadyDeletedIntegrationTest () {
    try {
      Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
      multipleTypeAsset.setIsDeleted(true);
      attributeRepository.save(multipleTypeAsset);

      assertThrows(AttributeNotFoundException.class, () -> attributesService.deleteAttributeById(multipleTypeAsset.getAttributeId(), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAttributeByIdDeleteAssetLinkUsageIntegrationTest () {
    Attribute attribute = attributeRepository.save(new Attribute(textAttributeType, differentAsset, language, user));
    AssetLinkUsage assetLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(attribute, differentAsset, user));

    attributesService.deleteAttributeById(attribute.getAttributeId(), user);

    assertAll(
      () -> assertEquals(0, assetLinkUsageRepository.findAllByParams(null, attribute.getAttributeId()).size()),
      () -> assertTrue(assetLinkUsageRepository.findById(assetLinkUsage.getAssetLinkUsageId()).get().getIsDeleted())
    );
  }

  @Test
  public void deleteAttributeByIdDeleteImagesLinkUsageIntegrationTest () {
    Attribute attribute = attributeRepository.save(new Attribute(textAttributeType, differentAsset, language, user));
    ImageLinkUsage imageLinkUsage = imageLinkUsageRepository.save(new ImageLinkUsage(UUID.randomUUID().toString(), attribute, user));

    attributesService.deleteAttributeById(attribute.getAttributeId(), user);

    assertAll(
      () -> assertEquals(0, imageLinkUsageRepository.findAllByAttributeId(attribute.getAttributeId()).size()),
      () -> assertTrue(imageLinkUsageRepository.findById(imageLinkUsage.getAssetLinkUsageId()).get().getIsDeleted())
    );
  }

  @Test
  public void deleteAttributesBulkSuccessIntegrationTest () {
    Attribute attribute = attributeRepository.save(new Attribute(textAttributeType, differentAsset, language, user));
    Attribute multipleTypeAsset = attributeRepository.save(new Attribute(multipleListAttributeType, differentAsset, language, user));

    attributesService.deleteAttributesBulk(List.of(attribute.getAttributeId(), multipleTypeAsset.getAttributeId()), user);

    Optional<Attribute> deletedAttribute = attributeRepository.findById(attribute.getAttributeId());
    Optional<Attribute> deletedMultipleTypeAsset = attributeRepository.findById(multipleTypeAsset.getAttributeId());

    assertAll(
      () -> assertNotNull(deletedMultipleTypeAsset.get().getDeletedOn()),
      () -> assertTrue(deletedMultipleTypeAsset.get().getIsDeleted()),
      () -> assertNotNull(deletedAttribute.get().getDeletedOn()),
      () -> assertTrue(deletedAttribute.get().getIsDeleted())
    );
  }

  @Test
  public void deleteAttributesBulkAttributeHistoryIntegrationTest () {
    PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");
    PostAttributeRequest secondRequest = new PostAttributeRequest(textAttributeType.getAttributeTypeId().toString(), differentAsset.getAssetId(), "some value");
    List<PostAttributeRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    List<PostAttributeResponse> postResponse = attributesService.createAttributesBulk(requests, user);

    attributesService.deleteAttributesBulk(List.of(postResponse.get(0).getAttribute_id(), postResponse.get(1).getAttribute_id()), user);

    Optional<Attribute> firstDeletedAttribute = attributeRepository.findById(postResponse.get(0).getAttribute_id());
    Optional<Attribute> secondDeletedAttribute = attributeRepository.findById(postResponse.get(1).getAttribute_id());

    List<AttributeHistory> attributeHistories = attributeHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(4, attributeHistories.size(), "attribute history size"),
      () -> assertEquals(postResponse.get(0).getCreated_on(), attributeHistories.stream().filter(asset -> !asset.getIsDeleted() && asset.getAttributeId().equals(postResponse.get(0).getAttribute_id())).findFirst().get().getValidFrom(), "first post attribute history valid from"),
      () -> assertEquals(postResponse.get(1).getCreated_on(), attributeHistories.stream().filter(asset -> !asset.getIsDeleted() && asset.getAttributeId().equals(postResponse.get(1).getAttribute_id())).findFirst().get().getValidFrom(), "second post attribute history valid from"),
      () -> assertEquals(firstDeletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(asset -> asset.getIsDeleted() && asset.getAttributeId().equals(firstDeletedAttribute.get().getAttributeId())).findFirst().get().getValidFrom(), "first deleted attribute history valid from"),
      () -> assertEquals(secondDeletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(asset -> asset.getIsDeleted() && asset.getAttributeId().equals(secondDeletedAttribute.get().getAttributeId())).findFirst().get().getValidFrom(), "second deleted attribute history valid from"),
      () -> assertEquals(firstDeletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(asset -> asset.getValue().equals(postResponse.get(0).getValue())).findFirst().get().getValidTo(), "first post attribute history valid to"),
      () -> assertEquals(secondDeletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(asset -> asset.getValue().equals(postResponse.get(1).getValue())).findFirst().get().getValidTo(), "second post attribute history valid to"),
      () -> assertEquals(firstDeletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(asset -> asset.getIsDeleted() && asset.getAttributeId().equals(firstDeletedAttribute.get().getAttributeId())).findFirst().get().getValidTo(), "first deleted attribute history valid to"),
      () -> assertEquals(secondDeletedAttribute.get().getDeletedOn(), attributeHistories.stream().filter(asset -> asset.getIsDeleted() && asset.getAttributeId().equals(secondDeletedAttribute.get().getAttributeId())).findFirst().get().getValidTo(), "second deleted attribute history valid to")
    );
  }

  @Test
  public void deleteAttributesBulkAttributeNotFoundIntegrationTest () {
    assertThrows(AttributeNotFoundException.class, () -> attributesService.deleteAttributesBulk(List.of(UUID.randomUUID()), user));
  }

  @Test
  public void deleteAttributesBulkAttributeAlreadyDeletedIntegrationTest () {
    Attribute attribute = attributeRepository.save(new Attribute(textAttributeType, differentAsset, language, user));
    Attribute multipleTypeAsset = new Attribute(multipleListAttributeType, differentAsset, language, user);
    multipleTypeAsset.setIsDeleted(true);
    attributeRepository.save(multipleTypeAsset);

    assertThrows(AttributeNotFoundException.class, () -> attributesService.deleteAttributesBulk(List.of(attribute.getAttributeId(), multipleTypeAsset.getAttributeId()), user));
  }

  @Test
  public void deleteAttributesBulkDeleteAssetLinkUsageIntegrationTest () {
    Attribute attribute = attributeRepository.save(new Attribute(textAttributeType, differentAsset, language, user));
    Attribute multipleTypeAsset = attributeRepository.save(new Attribute(multipleListAttributeType, differentAsset, language, user));
    AssetLinkUsage assetLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(attribute, differentAsset, user));

    attributesService.deleteAttributesBulk(List.of(attribute.getAttributeId(), multipleTypeAsset.getAttributeId()), user);

    assertAll(
      () -> assertEquals(0, assetLinkUsageRepository.findAllByParams(null, attribute.getAttributeId()).size()),
      () -> assertTrue(assetLinkUsageRepository.findById(assetLinkUsage.getAssetLinkUsageId()).get().getIsDeleted())
    );
  }

  @Test
  public void deleteAttributesBulkDeleteImagesLinkUsageIntegrationTest () {
    Attribute attribute = attributeRepository.save(new Attribute(textAttributeType, differentAsset, language, user));
    Attribute multipleTypeAsset = attributeRepository.save(new Attribute(multipleListAttributeType, differentAsset, language, user));
    ImageLinkUsage imageLinkUsage = imageLinkUsageRepository.save(new ImageLinkUsage(UUID.randomUUID().toString(), attribute, user));

    attributesService.deleteAttributesBulk(List.of(attribute.getAttributeId(), multipleTypeAsset.getAttributeId()), user);

    assertAll(
      () -> assertEquals(0, imageLinkUsageRepository.findAllByAttributeId(attribute.getAttributeId()).size()),
      () -> assertTrue(imageLinkUsageRepository.findById(imageLinkUsage.getAssetLinkUsageId()).get().getIsDeleted())
    );
  }

  private void generateAttributes (int count) {
    for (int i = 0; i < count; i++) {
      AttributeType attributeType = attributeTypeRepository.save(new AttributeType("boolean attribute type " + i, "attribute with boolean type " + i, AttributeKindType.BOOLEAN, null, null, language, user));

      attributeRepository.save(new Attribute(attributeType, differentAsset, language, user));
    }
  }
}
