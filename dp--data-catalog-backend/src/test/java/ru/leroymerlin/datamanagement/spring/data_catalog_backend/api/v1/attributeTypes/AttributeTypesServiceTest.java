package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.AttributeTypeAllowedValueRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.RelationAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.RelationComponentAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeValueAlreadyAssignedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.AttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PatchAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class AttributeTypesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private AttributeTypesService attributeTypesService;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;

  @Autowired
  private EntityRepository entityRepository;
  @Autowired
  private ActionTypeRepository actionTypeRepository;
  @Autowired
  private RoleActionRepository roleActionRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private AttributeRepository attributeRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private RelationAttributeRepository relationAttributeRepository;
  @Autowired
  private RelationComponentAttributeRepository relationComponentAttributeRepository;
  @Autowired
  private AttributeTypeAllowedValueRepository attributeTypeAllowedValueRepository;
  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  @Autowired
  private RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;
  @Autowired
  private RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;
  @Autowired
  private AssetTypeCardHeaderAssignmentRepository assetTypeCardHeaderAssignmentRepository;

  AssetType assetType;
  Asset asset;
  RelationType relationType;
  RelationTypeComponent relationTypeComponent;

  @BeforeAll
  public void createData () {
    relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));
    relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("relation type component", "desc", null, null, null, language, relationType, user));
  }

  @AfterAll
  public void clearData () {
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
  }

  @BeforeEach
  public void prepareData () {
    assetType = assetTypeRepository.save(new AssetType("some asset type", "some desc", "sat", "green", language, user));
    asset = assetRepository.save(new Asset("some name", assetType, "that", language, null, null, user));
  }

  @AfterEach
  public void clearTables () {
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    relationTypeAttributeTypeAssignmentRepository.deleteAll();
    relationTypeComponentAttributeTypeAssignmentRepository.deleteAll();
    relationAttributeRepository.deleteAll();
    relationComponentAttributeRepository.deleteAll();
    assetTypeCardHeaderAssignmentRepository.deleteAll();
    attributeRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    attributeTypeAllowedValueRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    roleActionRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  public void createAttributeTypeMultipleValueListIntegrationTest () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("test name", "test description", AttributeKindType.MULTIPLE_VALUE_LIST, null, List.of("1", "2"), null);
    PostAttributeTypeResponse attributeType = attributeTypesService.createAttributeType(request, user);

    assertAll(
      () -> assertEquals(request.getAttribute_type_name(), attributeType.getAttribute_type_name()),
      () -> assertEquals(2, attributeTypeAllowedValueRepository.countAttributeTypeAllowedValuesByAttributeTypeId(attributeType.getAttribute_type_id()))
    );
  }

  @Test
  public void createAttributeTypeMultipleValueListWithSameValueIntegrationTest () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("test name", "test description", AttributeKindType.MULTIPLE_VALUE_LIST, null, List.of("2", "2"), null);

    assertThrows(AttributeTypeValueAlreadyAssignedException.class, () -> attributeTypesService.createAttributeType(request, user));
  }

  @Test
  public void createAttributeTypeWithExistingAttributeTypeNameIntegrationTest () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("test name", "test description", AttributeKindType.MULTIPLE_VALUE_LIST, null, List.of("1", "2"), null);
    attributeTypeRepository.save(new AttributeType(request.getAttribute_type_name(), request.getAttribute_type_description(), request.getAttribute_type_kind(), null, null, language, user));

    assertThrows(DataIntegrityViolationException.class, () -> attributeTypesService.createAttributeType(request, user));
  }

  @Test
  public void updateAttributeTypeWithExistingAttributeTypeNameIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.of("test name"), Optional.of("test description"), null, null, null);
    attributeTypeRepository.save(new AttributeType(request.getAttribute_type_name().get(), request.getAttribute_type_description().get(), AttributeKindType.TEXT, null, null, language, user));

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));

    assertThrows(DataIntegrityViolationException.class, () -> attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user));
  }

  @Test
  public void updateAttributeTypeSuccessIntegrationTest () throws InterruptedException {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.of("test name"), Optional.of("test description"), null, null, null);
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertAll(
      () -> assertEquals(request.getAttribute_type_name().get(), response.getAttribute_type_name()),
      () -> assertNotNull(response.getLast_modified_on()),
      () -> assertNull(response.getRdm_table_id())
    );

    Thread.sleep(4000);

    request.setAttribute_type_description(null);
    AttributeTypeResponse response2 = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertAll(
      () -> assertEquals(response.getAttribute_type_description(), response2.getAttribute_type_description()),
      () -> assertNotEquals(response.getLast_modified_on(), response2.getLast_modified_on())
    );

    request.setRdm_table_id(Optional.of("some value"));
    AttributeTypeResponse response3 = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertAll(
      () -> assertNotEquals(response.getRdm_table_id(), response3.getRdm_table_id()),
      () -> assertEquals(response.getAttribute_type_name(), response3.getAttribute_type_name())
    );
  }

  @Test
  public void updateAttributeTypeClearNameIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.empty(), Optional.of("test description"), null, null, null);
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertEquals(attributeType.getAttributeTypeName(), response.getAttribute_type_name());
  }

  @Test
  public void updateAttributeTypeDoNothingWithNameIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(null, Optional.of("test description"), null, null, null);
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertEquals(attributeType.getAttributeTypeName(), response.getAttribute_type_name());
  }

  @Test
  public void updateAttributeTypeClearDescriptionIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.of("test name"), Optional.empty(), null, null, null);
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertNull(response.getAttribute_type_description());
  }

  @Test
  public void updateAttributeTypeDoNothingWithDescriptionIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.of("test name"), null, null, null, null);
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertEquals("some description", response.getAttribute_type_description());
  }

  @Test
  public void updateAttributeTypeClearRdmTableIdIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.of("test name"), Optional.empty(), null, null, Optional.empty());
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, "some id", language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertNull(response.getRdm_table_id());
  }

  @Test
  public void updateAttributeTypeDoNothingWithRdmTableIdIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.of("test name"), null, null, null, null);
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, "some id", language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertEquals("some id", response.getRdm_table_id());
  }

  @Test
  public void updateAttributeTypeClearValidationMaskIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.of("test name"), Optional.empty(), null, Optional.empty(), null);
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("hey you", "some description", AttributeKindType.TEXT,  "^hey", "some id", language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertNull(response.getValidation_mask());
  }

  @Test
  public void updateAttributeTypeDoNothingWithValidationMaskIntegrationTest () {
    PatchAttributeTypeRequest request = new PatchAttributeTypeRequest(Optional.of("test name"), null, null, null, null);
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("hey you", "some description", AttributeKindType.TEXT, "^hey", "some id", language, user));

    AttributeTypeResponse response = attributeTypesService.updateAttributeType(attributeType.getAttributeTypeId(), request, user);

    assertEquals("^hey", response.getValidation_mask());
  }

  @Test
  public void updateAttributeTypeUpdateAttributeKindIntegrationTest () {
    PostAttributeTypeResponse attributeTypeWithRTFKind = createAttributeTypeWithRTFType();
    PostAttributeTypeResponse attributeTypeWithTextKind = createAttributeTypeWithTextType();
    PostAttributeTypeResponse attributeTypeWithDateKind = createAttributeTypeWithDateKind();
    PostAttributeTypeResponse attributeTypeWithBooleanKind = createAttributeTypeWithBooleanKind();
    PostAttributeTypeResponse attributeTypeWithDecimalKind = createAttributeTypeWithDecimalKind();
    PostAttributeTypeResponse attributeTypeWithIntegerKind = createAttributeTypeWithIntegerKind();
    PostAttributeTypeResponse attributeTypeWithDateTimeKind = createAttributeTypeWithDateTimeKind();
    PostAttributeTypeResponse attributeTypeWithSingleListValues = createAttributeTypeWithSingleListValues();
    PostAttributeTypeResponse attributeTypeWithMultipleListValues = createAttributeTypeWithMultipleListValues();
    PostAttributeTypeResponse attributeTypeWithMultipleTypeAndSingleValue = createAttributeTypeWithMultipleTypeAndSingleValue();

    assertAll(
      () -> assertThrows(IncompatibleAttributeKindException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithBooleanKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.DECIMAL, null, null), user)),
      () -> assertThrows(IncompatibleAttributeKindException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithDateTimeKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.DATE, null, null), user)),
      () -> assertDoesNotThrow(() -> attributeTypesService.updateAttributeType(attributeTypeWithDateKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.DATE_TIME, null, null), user)),
      () -> assertThrows(IncompatibleAttributeKindException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithDateKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.INTEGER, null, null), user)),
      () -> assertThrows(IncompatibleAttributeKindException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithIntegerKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.DATE, null, null), user)),
      () -> assertDoesNotThrow(() -> attributeTypesService.updateAttributeType(attributeTypeWithIntegerKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.DECIMAL, null, null), user)),
      () -> assertThrows(IncompatibleAttributeKindException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithDecimalKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.SINGLE_VALUE_LIST, null, null), user)),
      () -> assertThrows(IncompatibleAttributeKindException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithSingleListValues.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.DECIMAL, null, null), user)),
      () -> assertDoesNotThrow(() -> attributeTypesService.updateAttributeType(attributeTypeWithSingleListValues.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.MULTIPLE_VALUE_LIST, null, null), user)),
      () -> assertThrows(IncompatibleAttributeKindException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithMultipleListValues.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.SINGLE_VALUE_LIST, null, null), user)),
      () -> assertDoesNotThrow(() -> attributeTypesService.updateAttributeType(attributeTypeWithMultipleTypeAndSingleValue.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.SINGLE_VALUE_LIST, null, null), user)),
      () -> assertThrows(IncompatibleAttributeKindException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithRTFKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.TEXT, null, null), user)),
      () -> assertDoesNotThrow(() -> attributeTypesService.updateAttributeType(attributeTypeWithDateKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.TEXT, null, null), user)),
      () -> assertDoesNotThrow(() -> attributeTypesService.updateAttributeType(attributeTypeWithTextKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.RTF, null, null), user))
    );
  }

  @Test
  public void updateAttributeTypeUpdateAttributeKindTextIntegrationTest () {
    PostAttributeTypeResponse attributeTypeWithRTFKind = createAttributeTypeWithRTFType();
    PostAttributeTypeResponse attributeTypeWithTextKind = createAttributeTypeWithTextType();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(attributeTypeWithTextKind.getAttribute_type_id());
    Attribute attribute = new Attribute(attributeType.get(), asset, language, user);
    attribute.setValue("puc");
    attributeRepository.save(attribute);

    assertAll(
      () -> assertThrows(ValidationMaskCantBeAppliedException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithRTFKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, AttributeKindType.DECIMAL, Optional.of("^hey"), null), user)),
      () -> assertThrows(AttributeDoesNotMatchTheMaskException.class, () -> attributeTypesService.updateAttributeType(attributeTypeWithTextKind.getAttribute_type_id(), new PatchAttributeTypeRequest(null, null, null, Optional.of("^hey"), null), user))
    );
  }

  @Test
  public void getAttributeTypeByIdSuccessIntegrationTest () {
    PostAttributeTypeResponse response = createAttributeTypeWithMultipleTypeAndSingleValue();
    Optional<AttributeType> optionalSingleListValues = attributeTypeRepository.findById(response.getAttribute_type_id());
    attributeRepository.save(new Attribute(optionalSingleListValues.get(), null, language, user));

    GetAttributeTypeResponse attributeTypeById = attributeTypesService.getAttributeTypeById(response.getAttribute_type_id());

    assertAll(
      () -> assertEquals(response.getAttribute_type_description(), attributeTypeById.getAttribute_type_description()),
      () -> assertEquals(1, attributeTypeById.getAttribute_type_usage_count())
    );
  }

  @Test
  public void getAttributeTypeByIdNotFoundAttributeTypeIntegrationTest () {
    assertThrows(AttributeTypeNotFoundException.class, () -> attributeTypesService.getAttributeTypeById(new UUID(123, 12)));
  }

  @Test
  public void getAttributeTypeByParamsPaginationIntegrationTest () {
    generateAttributeTypes(130);

    assertAll(
      () -> assertEquals(50, attributeTypesService.getAttributeTypeByParams(null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(100, attributeTypesService.getAttributeTypeByParams(null, null, null, 0, 140).getResults().size()),
      () -> assertEquals(1, attributeTypesService.getAttributeTypeByParams("110", null, null, 0, 50).getResults().size()),
      () -> assertEquals(2, attributeTypesService.getAttributeTypeByParams("1", null, null, 0, 2).getResults().size()),
      () -> assertEquals(11, attributeTypesService.getAttributeTypeByParams("11", null, null, 0, 2).getTotal()),
      () -> assertEquals(130, attributeTypesService.getAttributeTypeByParams(null, null, null, 0, 50).getTotal())
    );
  }

  @Test
  public void getAttributeTypeByParamsIntegrationTest () {
    createAttributeTypeWithMultipleListValues();
    PostAttributeTypeResponse singleListValues = createAttributeTypeWithSingleListValues();
    createAttributeTypeWithMultipleTypeAndSingleValue();
    PostAttributeTypeResponse integerKind = createAttributeTypeWithIntegerKind();

    Optional<AttributeType> optionalSingleListValues = attributeTypeRepository.findById(singleListValues.getAttribute_type_id());
    attributeRepository.save(new Attribute(optionalSingleListValues.get(), null, language, user));

    Optional<AttributeType> optionalIntegerKind = attributeTypeRepository.findById(integerKind.getAttribute_type_id());
    attributeRepository.save(new Attribute(optionalIntegerKind.get(), null, language, user));

    GetAttributeTypesResponse allAttributeTypesResponse = attributeTypesService.getAttributeTypeByParams(null, null, null, 0, 50);
    GetAttributeTypesResponse multipleTypesResponse = attributeTypesService.getAttributeTypeByParams("multiple", null, null, 0, 50);
    GetAttributeTypesResponse singleTypeResponse = attributeTypesService.getAttributeTypeByParams(null, "gle", null, 0, 50);
    GetAttributeTypesResponse emptyResponse = attributeTypesService.getAttributeTypeByParams(null, "gle", AttributeKindType.INTEGER, 0, 50);
    GetAttributeTypesResponse integerTypeResponse = attributeTypesService.getAttributeTypeByParams(null, null, AttributeKindType.INTEGER, 0, 50);

    assertAll(
      () -> assertEquals(4, allAttributeTypesResponse.getResults().size()),
      () -> assertEquals(2, multipleTypesResponse.getResults().size()),
      () -> assertEquals(1, singleTypeResponse.getResults().size()),
      () -> assertEquals(singleListValues.getAttribute_type_id(), singleTypeResponse.getResults().get(0).getAttribute_type_id()),
      () -> assertEquals(1, singleTypeResponse.getResults().get(0).getAttribute_type_usage_count()),
      () -> assertEquals(0, emptyResponse.getResults().size()),
      () -> assertEquals(integerKind.getAttribute_type_id(), integerTypeResponse.getResults().get(0).getAttribute_type_id()),
      () -> assertEquals(1, integerTypeResponse.getResults().get(0).getAttribute_type_usage_count())
    );
  }

  @Test
  public void deleteAttributeTypeByIdSuccessIntegrationTest () {
    PostAttributeTypeResponse singleListValues = createAttributeTypeWithSingleListValues();

    attributeTypesService.deleteAttributeTypeById(singleListValues.getAttribute_type_id(), user);
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(singleListValues.getAttribute_type_id());

    assertAll(
      () -> assertTrue(attributeType.get().getIsDeleted()),
      () -> assertEquals(user.getUserId(), attributeType.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAttributeTypeByIdAttributeTypeNotFoundIntegrationTest () {
    assertThrows(AttributeTypeNotFoundException.class, () -> attributeTypesService.deleteAttributeTypeById(new UUID(123, 123), user));
  }

  @Test
  public void deleteAttributeTypeByIdHasConnectedAttributesIntegrationTest () {
    PostAttributeTypeResponse booleanKind = createAttributeTypeWithBooleanKind();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(booleanKind.getAttribute_type_id());
    attributeRepository.save(new Attribute(attributeType.get(), null, language, user));

    assertThrows(AttributeWithAttributeTypeExistsException.class, () -> attributeTypesService.deleteAttributeTypeById(booleanKind.getAttribute_type_id(), user));
  }

  @Test
  public void deleteAttributeTypeByIdHasConnectedRelationAttributesIntegrationTest () {
    PostAttributeTypeResponse booleanKind = createAttributeTypeWithBooleanKind();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(booleanKind.getAttribute_type_id());

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType.get(), null, language, user));

    assertThrows(RelationAttributeWithAttributeTypeExistsException.class, () -> attributeTypesService.deleteAttributeTypeById(booleanKind.getAttribute_type_id(), user));
  }

  @Test
  public void deleteAttributeTypeByIdHasConnectedRelationComponentAttributesIntegrationTest () {
    PostAttributeTypeResponse booleanKind = createAttributeTypeWithBooleanKind();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(booleanKind.getAttribute_type_id());

    relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType.get(), null, language, user));

    assertThrows(RelationComponentAttributeWithAttributeTypeExistsException.class, () -> attributeTypesService.deleteAttributeTypeById(booleanKind.getAttribute_type_id(), user));
  }

  @Test
  public void deleteAttributeTypeByIdDeleteConnectedAssignmentsIntegrationTest () {
    PostAttributeTypeResponse booleanKind = createAttributeTypeWithBooleanKind();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(booleanKind.getAttribute_type_id());
    AssetTypeAttributeTypeAssignment attributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType.get(), user));

    attributeTypesService.deleteAttributeTypeById(booleanKind.getAttribute_type_id(), user);

    Optional<AssetTypeAttributeTypeAssignment> updatedAssignments = assetTypeAttributeTypeAssignmentRepository.findById(attributeTypeAssignment.getAssetTypeAttributeTypeAssignmentId());

    assertAll(
      () -> assertTrue(updatedAssignments.get().getIsDeleted()),
      () -> assertEquals(user.getUserId(), updatedAssignments.get().getDeletedBy().getUserId()),
      () -> assertNotNull(updatedAssignments.get().getDeletedOn())
    );
  }

  @Test
  public void deleteAttributeTypeByIdDeleteAllConnectedRoleActionsIntegrationTest () {
    PostAttributeTypeResponse singleListValues = createAttributeTypeWithSingleListValues();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(singleListValues.getAttribute_type_id());

    Optional<Entity> entity = entityRepository.findById(UUID.fromString("360ef840-5c19-424b-a86f-b3e24c2fcc2f"));
    Optional<ActionType> actionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    RoleAction roleAction = new RoleAction(role, actionType.get(), entity.get(), ActionScopeType.ONE_ID, PermissionType.ALLOW, user);

    roleAction.setAttributeType(attributeType.get());
    roleActionRepository.save(roleAction);

    attributeTypesService.deleteAttributeTypeById(singleListValues.getAttribute_type_id(), user);

    Optional<RoleAction> deletedRoleAction = roleActionRepository.findById(roleAction.getRoleActionId());

    assertAll(
      () -> assertTrue(deletedRoleAction.get().getIsDeleted()),
      () -> assertNotNull(deletedRoleAction.get().getDeletedOn())
    );
  }

  @Test
  public void deleteAttributeTypeByIdDeleteAllConnectedRelationTypeComponentAttributeTypeAssignmentsIntegrationTest () {
    PostAttributeTypeResponse singleListValues = createAttributeTypeWithSingleListValues();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(singleListValues.getAttribute_type_id());

    RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType.get(), user));

    attributeTypesService.deleteAttributeTypeById(singleListValues.getAttribute_type_id(), user);

    Optional<RelationTypeComponentAttributeTypeAssignment> deletedAssignment = relationTypeComponentAttributeTypeAssignmentRepository.findById(relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn())
    );
  }

  @Test
  public void deleteAttributeTypeByIdDeleteAllConnectedRelationComponentAttributeTypeAssignmentsIntegrationTest () {
    PostAttributeTypeResponse singleListValues = createAttributeTypeWithSingleListValues();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(singleListValues.getAttribute_type_id());

    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType.get(), user));

    attributeTypesService.deleteAttributeTypeById(singleListValues.getAttribute_type_id(), user);

    Optional<RelationTypeAttributeTypeAssignment> deletedAssignment = relationTypeAttributeTypeAssignmentRepository.findById(relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn())
    );
  }

  @Test
  public void deleteAttributeTypeByIdDeleteAllConnectedAssetTypeCardHeaderAssignmentsIntegrationTest () {
    PostAttributeTypeResponse singleListValues = createAttributeTypeWithSingleListValues();
    Optional<AttributeType> attributeType = attributeTypeRepository.findById(singleListValues.getAttribute_type_id());

    AssetTypeCardHeaderAssignment assignment = assetTypeCardHeaderAssignmentRepository.save(new AssetTypeCardHeaderAssignment(assetType, attributeType.get(), null, user));

    attributeTypesService.deleteAttributeTypeById(singleListValues.getAttribute_type_id(), user);

    Optional<AssetTypeCardHeaderAssignment> deletedAssignment = assetTypeCardHeaderAssignmentRepository.findById(assignment.getAssetTypeCardHeaderAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn())
    );
  }

  public PostAttributeTypeResponse createAttributeTypeWithRTFType () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("rtf type", "rtf text type", AttributeKindType.RTF, null, null, null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithTextType () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("text type", "test text type", AttributeKindType.TEXT, null, null, null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithMultipleTypeAndSingleValue () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("multiple type one value", "test multiple type with one value", AttributeKindType.MULTIPLE_VALUE_LIST, null, List.of("multiple_0"), null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithMultipleListValues () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("multiple values", "test multiple values", AttributeKindType.MULTIPLE_VALUE_LIST, null, List.of("multiple_1", "multiple_2"), null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithSingleListValues () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("single value", "test single value", AttributeKindType.SINGLE_VALUE_LIST, null, List.of("single"), null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithDecimalKind () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("decimal", "test decimal", AttributeKindType.DECIMAL, null, null, null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithIntegerKind () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("integer", "test integer", AttributeKindType.INTEGER, null, null, null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithDateKind () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("date", "test date", AttributeKindType.DATE, null, null, null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithDateTimeKind () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("datetime", "test datetime", AttributeKindType.DATE_TIME, null, null, null);

    return attributeTypesService.createAttributeType(request, user);
  }

  public PostAttributeTypeResponse createAttributeTypeWithBooleanKind () {
    PostAttributeTypeRequest request = new PostAttributeTypeRequest("boolean", "test boolean", AttributeKindType.DATE_TIME, null, null, null);

    return attributeTypesService.createAttributeType(request, user);
  }

  private void generateAttributeTypes (int count) {
    for (int i = 0; i < count; i++) {
      attributeTypeRepository.save(new AttributeType("attribute type " + i, "description " + i, AttributeKindType.TEXT, null, null, language, user));
    }
  }
}
