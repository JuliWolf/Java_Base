package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.AssetTypeAttributeTypesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypeInheritance.AssetTypeInheritanceRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.AttributeTypeAllowedValueRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AttributeTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributeTypeAssignmentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributeTypesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class AssetTypeAttributeTypesAssignmentsServiceTest extends ServiceWithUserIntegrationTest {

  @Autowired
  private AssetTypeAttributeTypesAssignmentsService assetTypeAttributeTypesAssignmentsService;

  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AttributeRepository attributeRepository;
  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetTypeInheritanceRepository assetTypeInheritanceRepository;
  @Autowired
  private AttributeTypeAllowedValueRepository attributeTypeAllowedValueRepository;

  private AttributeType attributeType;
  private AssetType assetType;

  private Asset asset;

  private Attribute attribute;

  @BeforeAll
  public void prepareAssetTypesAndStatuses () {
    attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "attribute type description", AttributeKindType.DATE, null, null, language, user));
    assetType = assetTypeRepository.save(new AssetType("asset type name", "description", "atn", "red", language, user));
    asset = assetRepository.save(new Asset("test", assetType, "some name", language, null, null, user));
    attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
  }

  @AfterAll
  public void clearAssetTypesAndStatuses () {
    assetTypeInheritanceRepository.deleteAll();
    attributeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearData () {
    attributeTypeAllowedValueRepository.deleteAll();
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
  }

  @Test
  public void createAssetTypeAttributeTypesAssignmentsSuccessIntegrationTest () {
    List<PostAssetTypeAttributeTypeAssignmentRequest> attributesList = new ArrayList<>();
    attributesList.add(new PostAssetTypeAttributeTypeAssignmentRequest(attributeType.getAttributeTypeId()));

    PostAssetTypeAttributeTypesAssignmentsRequest request = new PostAssetTypeAttributeTypesAssignmentsRequest(attributesList);
    PostAssetTypeAttributesAssignmentsResponse assignments = assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(assetType.getAssetTypeId(), request, user);

    assertAll(
      () -> assertEquals(1, assignments.getAsset_attribute_assignment().size()),
      () -> assertEquals(attributeType.getAttributeTypeId(), assignments.getAsset_attribute_assignment().get(0).getAttribute_type_id())
    );
  }

  @Test
  public void createAssetTypeAttributeTypesAssignmentsAttributeNotFoundIntegrationTest () {
    List<PostAssetTypeAttributeTypeAssignmentRequest> attributesList = new ArrayList<>();
    attributesList.add(new PostAssetTypeAttributeTypeAssignmentRequest(new UUID(123, 123)));

    PostAssetTypeAttributeTypesAssignmentsRequest request = new PostAssetTypeAttributeTypesAssignmentsRequest(attributesList);

    assertThrows(AttributeTypeNotFoundException.class, () -> assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(assetType.getAssetTypeId(), request, user));
  }

  @Test
  public void createAssetTypeAttributeTypesAssignmentsAssetTypeNotFoundIntegrationTest () {
    List<PostAssetTypeAttributeTypeAssignmentRequest> attributesList = new ArrayList<>();
    attributesList.add(new PostAssetTypeAttributeTypeAssignmentRequest(new UUID(123, 123)));

    PostAssetTypeAttributeTypesAssignmentsRequest request = new PostAssetTypeAttributeTypesAssignmentsRequest(attributesList);

    assertThrows(AssetTypeNotFoundException.class, () -> assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(new UUID(123, 123), request, user));
  }

  @Test
  public void createAssetTypeAttributeTypesAssignmentsAssignmentAlreadyExistsIntegrationTest () {
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));
    List<PostAssetTypeAttributeTypeAssignmentRequest> attributesList = new ArrayList<>();
    attributesList.add(new PostAssetTypeAttributeTypeAssignmentRequest(attributeType.getAttributeTypeId()));

    PostAssetTypeAttributeTypesAssignmentsRequest request = new PostAssetTypeAttributeTypesAssignmentsRequest(attributesList);

    assertThrows(DataIntegrityViolationException.class, () -> assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(assetType.getAssetTypeId(), request, user));
  }

  @Test
  public void createAssetTypeAttributeTypesCreateAssignmentsForChildrenInTreeIntegrationTest () {
    List<PostAssetTypeAttributeTypeAssignmentRequest> attributesList = new ArrayList<>();
    attributesList.add(new PostAssetTypeAttributeTypeAssignmentRequest(attributeType.getAttributeTypeId()));

    AssetType BAssetType = assetTypeRepository.save(new AssetType("child asset type name", "description", "atn", "red", language, user));
    assetTypeInheritanceRepository.save(new AssetTypeInheritance(assetType, BAssetType, user));

    AssetType CAssetType = assetTypeRepository.save(new AssetType("C asset type name", "description", "atn", "red", language, user));
    assetTypeInheritanceRepository.save(new AssetTypeInheritance(BAssetType, CAssetType, user));

    assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(assetType.getAssetTypeId(), new PostAssetTypeAttributeTypesAssignmentsRequest(attributesList), user);

    List<AssetTypeAttributeTypeAssignment> assignments = assetTypeAttributeTypeAssignmentRepository.findAll();
    List<AssetTypeAttributeTypeAssignment> inheritedAssignments = assignments.stream().filter(AssetTypeAttributeTypeAssignment::getIsInherited).toList();

    assertAll(
      () -> assertNotEquals(0, inheritedAssignments.size()),
      () -> assertEquals(assetType.getAssetTypeId(), inheritedAssignments.get(0).getParentAssetType().getAssetTypeId())
    );
  }

  @Test
  public void getAssetTypeAttributeTypesAssignmentsByAssetTypeIdAllowedValuesIntegrationTest () {
    AssetType assetType2 = assetTypeRepository.save(new AssetType("asset 2 type name 2", "description", "atn", "red", language, user));
    AttributeType attributeType2 = attributeTypeRepository.save(new AttributeType("attribute2 type name 2", "attribute2 type description", AttributeKindType.DATE, null, null, language, user));
    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(attributeType2, "1", language, user));
    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(attributeType2, "2", language, user));
    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(attributeType2, "3", language, user));

    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType2, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType2, attributeType2, user));

    GetAssetTypeAttributeTypesAssignmentsResponse secondAssetTypeResult = assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByAssetTypeId(assetType2.getAssetTypeId());

    assertAll(
      () -> assertEquals("123", String.join("",secondAssetTypeResult.getAsset_attribute_assignment().get(0).getAllowed_values()))
    );
  }

  @Test
  public void getAssetTypeAttributeTypesAssignmentsByAssetTypeIdSuccessIntegrationTest () {
    AssetType assetType2 = assetTypeRepository.save(new AssetType("asset 2 type name", "description", "atn", "red", language, user));
    AttributeType attributeType2 = attributeTypeRepository.save(new AttributeType("attribute2 type name", "attribute2 type description", AttributeKindType.DATE, null, null, language, user));

    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType2, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType2, attributeType2, user));

    GetAssetTypeAttributeTypesAssignmentsResponse firstAssetTypeResult = assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByAssetTypeId(assetType.getAssetTypeId());
    GetAssetTypeAttributeTypesAssignmentsResponse secondAssetTypeResult = assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByAssetTypeId(assetType2.getAssetTypeId());

    assertAll(
      () -> assertEquals(2, firstAssetTypeResult.getAsset_attribute_assignment().size()),
      () -> assertEquals(1, secondAssetTypeResult.getAsset_attribute_assignment().size())
    );
  }

  @Test
  public void getAssetTypeAttributeTypesAssignmentsByAssetTypeIdAssetTypeNotFoundIntegrationTest () {
    assertThrows(AssetTypeNotFoundException.class, () -> assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByAssetTypeId(new UUID(123, 123)));
  }

  @Test
  public void getAssetTypeAttributeTypesAssignmentsByParams_illegalArgumentException_integrationTest () {
    assertThrows(IllegalArgumentException.class, () -> assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams("123", "123", null, null, 0, 50));
  }

  @Test
  public void getAssetTypeAttributeTypesAssignmentsByParams_assetTypeNotFoundException_integrationTest () {
    assertThrows(AssetTypeNotFoundException.class, () -> assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(UUID.randomUUID().toString(), "123", null, null, 0, 50));
  }

  @Test
  public void getAssetTypeAttributeTypesAssignmentsByParams_successTest_integrationTest () {
    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType("1 first attribute type name", "attribute type description", AttributeKindType.DATE, null, null, language, user));
    AssetType firstAssetType = assetTypeRepository.save(new AssetType("1 first asset type name", "description", "atn", "red", language, user));

    AttributeType secondAttributeType = attributeTypeRepository.save(new AttributeType("2 second attribute type name", "attribute type description", AttributeKindType.DATE, null, null, language, user));
    AssetType secondAssetType = assetTypeRepository.save(new AssetType("2 second asset type name", "description", "atn", "red", language, user));

    AssetTypeAttributeTypeAssignment assignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));
    AssetTypeAttributeTypeAssignment firstAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(firstAssetType, firstAttributeType, user));
    AssetTypeAttributeTypeAssignment secondAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(secondAssetType, secondAttributeType, user));
    AssetTypeAttributeTypeAssignment thirdAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, firstAttributeType, user));
    AssetTypeAttributeTypeAssignment forthAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, secondAttributeType, user));
    AssetTypeAttributeTypeAssignment fifthAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(firstAssetType, attributeType, user));
    AssetTypeAttributeTypeAssignment sixthAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(secondAssetType, attributeType, user));

    assertAll(
      () -> assertEquals(7, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(null, null, null, null, 0, 50).getTotal()),
      () -> assertEquals(1, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(null, null, SortField.ASSET_TYPE_ATTRIBUTE_TYPE_USAGE_COUNT, SortOrder.DESC, 0, 50).getResults().get(0).getAsset_type_attribute_type_usage_count()),
      () -> assertEquals(firstAssetType.getAssetTypeName(), assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(null, null, SortField.ASSET_TYPE_NAME, null, 0, 50).getResults().get(0).getAsset_type_name()),
      () -> assertEquals(firstAttributeType.getAttributeTypeName(), assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(null, null, SortField.ATTRIBUTE_TYPE_NAME, null, 0, 50).getResults().get(0).getAttribute_type_name()),
      () -> assertEquals(3, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(assetType.getAssetTypeId().toString(), null, null, null, 0, 50).getTotal()),
      () -> assertEquals(1, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(assetType.getAssetTypeId().toString(), attributeType.getAttributeTypeId().toString(), null, null, 0, 50).getTotal()),
      () -> assertEquals(1, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(assetType.getAssetTypeId().toString(), attributeType.getAttributeTypeId().toString(), null, null, 0, 50).getResults().get(0).getAsset_type_attribute_type_usage_count()),
      () -> assertEquals(2, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(secondAssetType.getAssetTypeId().toString(), null, null, null, 0, 50).getTotal()),
      () -> assertEquals(2, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(firstAssetType.getAssetTypeId().toString(), null, null, null, 0, 50).getTotal()),
      () -> assertEquals(1, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(firstAssetType.getAssetTypeId().toString(), attributeType.getAttributeTypeId().toString(), null, null, 0, 50).getTotal()),
      () -> assertEquals(3, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(null, attributeType.getAttributeTypeId().toString(), null, null, 0, 50).getTotal()),
      () -> assertEquals(2, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(null, firstAttributeType.getAttributeTypeId().toString(), null, null, 0, 50).getTotal()),
      () -> assertEquals(2, assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(null, secondAttributeType.getAttributeTypeId().toString(), null, null, 0, 50).getTotal())
    );
  }

  @Test
  public void deleteAssetTypeAttributeTypeAssignmentByIdAssetTypeAttributeTypeAssignmentNotFoundIntegrationTest () {
    assertThrows(AssetTypeAttributeTypeAssignmentNotFoundException.class, () -> assetTypeAttributeTypesAssignmentsService.deleteAssetTypeAttributeTypeAssignmentById(new UUID(123, 123), user));
  }

  @Test
  public void deleteAssetTypeAttributeTypeAssignmentByIdAssetTypeAttributeTypeAssignmentAlreadyDeletedIntegrationTest () {
    AssetTypeAttributeTypeAssignment assignment = new AssetTypeAttributeTypeAssignment(assetType, attributeType, user);
    assignment.setIsDeleted(true);
    assetTypeAttributeTypeAssignmentRepository.save(assignment);

    assertThrows(AssetTypeAttributeTypeAssignmentNotFoundException.class, () -> assetTypeAttributeTypesAssignmentsService.deleteAssetTypeAttributeTypeAssignmentById(assignment.getAssetTypeAttributeTypeAssignmentId(), user));
  }

  @Test
  public void deleteAssetTypeAttributeTypeAssignmentByIdAttributeIsUsedForAssetIntegrationTest () {
    AssetTypeAttributeTypeAssignment assignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));

    assertThrows(
      AttributeTypeIsUsedForAssetException.class,
      () -> assetTypeAttributeTypesAssignmentsService.deleteAssetTypeAttributeTypeAssignmentById(assignment.getAssetTypeAttributeTypeAssignmentId(), user)
    );
  }

  @Test
  public void deleteAssetTypeAttributeTypeAssignmentByIdAssignmentsIsInheritedIntegrationTest () {
    AssetTypeAttributeTypeAssignment assignment = new AssetTypeAttributeTypeAssignment(assetType, attributeType, user);
    assignment.setIsInherited(true);
    assetTypeAttributeTypeAssignmentRepository.save(assignment);

    assertThrows(AssetTypeAttributeTypeAssignmentIsInheritedException.class, () -> assetTypeAttributeTypesAssignmentsService.deleteAssetTypeAttributeTypeAssignmentById(assignment.getAssetTypeAttributeTypeAssignmentId(), user));
  }

  @Test
  public void deleteAssetTypeAttributeTypeAssignmentByIdAttributeTypeIsUsedForAssetIntegrationTest () {
    AssetTypeAttributeTypeAssignment assignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));

    assertThrows(AttributeTypeIsUsedForAssetException.class, () -> assetTypeAttributeTypesAssignmentsService.deleteAssetTypeAttributeTypeAssignmentById(assignment.getAssetTypeAttributeTypeAssignmentId(), user));
  }

  @Test
  public void deleteAssetTypeAttributeTypeAssignmentByIdChildAttributeTypeIsUsedForAssetIntegrationTest () {
    AttributeType parentAttributeType = attributeTypeRepository.save(new AttributeType("test attribute type name", "attribute type description", AttributeKindType.DATE, null, null, language, user));
    AssetType parentAssetType = assetTypeRepository.save(new AssetType("test asset type name", "description", "atn", "red", language, user));
    AssetTypeAttributeTypeAssignment assignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(parentAssetType, parentAttributeType, user));

    Asset childAsset = assetRepository.save(new Asset("child asset", assetType, "display", language, null, null, user));
    Attribute childAttribute = attributeRepository.save(new Attribute(parentAttributeType, childAsset, language, user));
    AssetTypeAttributeTypeAssignment childAssignment = new AssetTypeAttributeTypeAssignment(assetType, parentAttributeType, user);
    childAssignment.setIsInherited(true);
    childAssignment.setParentAssetType(parentAssetType);
    assetTypeAttributeTypeAssignmentRepository.save(childAssignment);

    assertThrows(AttributeTypeIsUsedForAssetException.class, () -> assetTypeAttributeTypesAssignmentsService.deleteAssetTypeAttributeTypeAssignmentById(assignment.getAssetTypeAttributeTypeAssignmentId(), user));
  }
}
