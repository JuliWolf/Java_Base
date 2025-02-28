package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeCardHeaderAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.exceptions.AttributeTypeNotAssignedForAssetTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author juliwolf
 */

public class AssetTypeCardHeaderAssignmentServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private AssetTypeCardHeaderAssignmentService assetTypeCardHeaderAssignmentService;

  @Autowired
  private AssetTypeCardHeaderAssignmentRepository assetTypeCardHeaderAssignmentRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  @Autowired
  private RoleRepository roleRepository;

  Role role;
  AttributeType attributeType;
  AssetType assetType;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("some name", "desc", language, user));
    assetType = assetTypeRepository.save(new AssetType("asset type name", "desc", "acr", "color", language, user));
    attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));
  }

  @AfterAll
  public void clearData () {
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    roleRepository.deleteAll();
    assetTypeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAssignments () {
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    assetTypeCardHeaderAssignmentRepository.deleteAll();
  }

  @Test
  public void createAssetTypeCardHeaderAssignmentAssetTypeNotFoundIntegrationTest () {
    PostAssetTypeCardHeaderAssignmentRequest request = new PostAssetTypeCardHeaderAssignmentRequest(attributeType.getAttributeTypeId().toString(), role.getRoleId().toString());
    assertThrows(AssetTypeNotFoundException.class, () -> assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(UUID.randomUUID(), request, user));
  }

  @Test
  public void createAssetTypeCardHeaderAssignmentAttributeTypeNotFoundIntegrationTest () {
    PostAssetTypeCardHeaderAssignmentRequest request = new PostAssetTypeCardHeaderAssignmentRequest(UUID.randomUUID().toString(), role.getRoleId().toString());
    assertThrows(AttributeTypeNotFoundException.class, () -> assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(assetType.getAssetTypeId(), request, user));
  }

  @Test
  public void createAssetTypeCardHeaderAssignmentIllegalAttributeTypeIntegrationTest () {
    PostAssetTypeCardHeaderAssignmentRequest request = new PostAssetTypeCardHeaderAssignmentRequest("123", role.getRoleId().toString());
    assertThrows(IllegalArgumentException.class, () -> assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(assetType.getAssetTypeId(), request, user));
  }

  @Test
  public void createAssetTypeCardHeaderAssignmentRoleNotFoundIntegrationTest () {
    PostAssetTypeCardHeaderAssignmentRequest request = new PostAssetTypeCardHeaderAssignmentRequest(attributeType.getAttributeTypeId().toString(), UUID.randomUUID().toString());
    assertThrows(RoleNotFoundException.class, () -> assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(assetType.getAssetTypeId(), request, user));
  }

  @Test
  public void createAssetTypeCardHeaderAssignmentAttributeTypeNotAssignmentForAssetTypeIntegrationTest () {
    PostAssetTypeCardHeaderAssignmentRequest request = new PostAssetTypeCardHeaderAssignmentRequest(attributeType.getAttributeTypeId().toString(), role.getRoleId().toString());
    assertThrows(AttributeTypeNotAssignedForAssetTypeException.class, () -> assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(assetType.getAssetTypeId(), request, user));
  }

  @Test
  public void createAssetTypeCardHeaderAssignmentSuccessIntegrationTest () {
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));
    PostAssetTypeCardHeaderAssignmentRequest request = new PostAssetTypeCardHeaderAssignmentRequest(attributeType.getAttributeTypeId().toString(), role.getRoleId().toString());
    PostAssetTypeCardHeaderAssignmentResponse response = assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(assetType.getAssetTypeId(), request, user);

    assertAll(
      () -> assertEquals(assetType.getAssetTypeId(), response.getAsset_type_id()),
      () -> assertEquals(role.getRoleId(), response.getOwner_field_role_id()),
      () -> assertEquals(attributeType.getAttributeTypeId(), response.getDescription_field_attribute_type_id())
    );
  }

  @Test
  public void deleteAssetTypeCardHeaderAssignmentAssignmentNotFoundIntegrationTest () {
    assertThrows(AssetTypeCardHeaderAssignmentNotFoundException.class, () -> assetTypeCardHeaderAssignmentService.deleteAssetTypeCardHeaderAssignment(UUID.randomUUID(), user));
  }

  @Test
  public void deleteAssetTypeCardHeaderAssignmentAssignmentAlreadyDeletedIntegrationTest () {
    AssetTypeCardHeaderAssignment assignment = new AssetTypeCardHeaderAssignment(assetType, attributeType, role, user);
    assignment.setIsDeleted(true);
    assetTypeCardHeaderAssignmentRepository.save(assignment);

    assertThrows(AssetTypeCardHeaderAssignmentNotFoundException.class, () -> assetTypeCardHeaderAssignmentService.deleteAssetTypeCardHeaderAssignment(assignment.getAssetTypeCardHeaderAssignmentId(), user));
  }

  @Test
  public void deleteAssetTypeCardHeaderAssignmentSuccessIntegrationTest () {
    AssetTypeCardHeaderAssignment assignment = assetTypeCardHeaderAssignmentRepository.save(new AssetTypeCardHeaderAssignment(assetType, attributeType, role, user));
    assetTypeCardHeaderAssignmentService.deleteAssetTypeCardHeaderAssignment(assignment.getAssetTypeCardHeaderAssignmentId(), user);

    Optional<AssetTypeCardHeaderAssignment> deletedAssignment = assetTypeCardHeaderAssignmentRepository.findById(assignment.getAssetTypeCardHeaderAssignmentId());

    assertAll(
      () -> assertNotNull(deletedAssignment.get().getDeletedOn()),
      () -> assertTrue(deletedAssignment.get().getIsDeleted())
    );
  }
}
