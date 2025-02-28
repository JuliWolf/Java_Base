package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.GetAssetHeaderResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostAssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.AssetHierarchyRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHistory.AssetHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetLinkUsage.AssetLinkUsageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.AssetTypeStatusAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeHistory.AttributeHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.CustomViewRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.RelationAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributesHistory.RelationComponentAttributesHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.SubscriptionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class AssetsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AssetsService assetsService;

  @Autowired
  private AttributesService attributesService;

  @Autowired
  private RelationAttributesService relationAttributesService;

  @Autowired
  private RelationComponentAttributesService relationComponentAttributesService;

  @Autowired
  private AssetHierarchyRepository assetHierarchyRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AttributeRepository attributeRepository;
  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  @Autowired
  private AssetTypeStatusAssignmentRepository assetTypeStatusAssignmentRepository;
  @Autowired
  private RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;
  @Autowired
  private RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;
  @Autowired
  private StatusRepository statusRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private ResponsibilityRepository responsibilityRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private RelationComponentRepository relationComponentRepository;
  @Autowired
  private AssetLinkUsageRepository assetLinkUsageRepository;
  @Autowired
  private AssetTypeCardHeaderAssignmentRepository assetTypeCardHeaderAssignmentRepository;
  @Autowired
  private CustomViewRepository customViewRepository;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private AssetHistoryRepository assetHistoryRepository;
  @Autowired
  private AttributeHistoryRepository attributeHistoryRepository;
  @Autowired
  private RelationAttributeRepository relationAttributeRepository;
  @Autowired
  private RelationComponentAttributesHistoryRepository relationComponentAttributesHistoryRepository;

  @Autowired
  private EntityManager entityManager;

  Status lifecycleStatus = null;
  Status stewardshipStatus = null;
  Status statusWithNoAssignment = null;

  AssetType assetTypeOne = null;
  AssetType assetTypeTwo = null;

  AssetTypeStatusAssignment lifecycleAssignment = null;
  AssetTypeStatusAssignment stewardshipAssignment = null;

  @BeforeEach
  public void prepareData () {
    assetHistoryRepository.deleteAll();

    assetTypeOne = assetTypeRepository.save(new AssetType("first asset type", "first asset Type description", "fat", "red", language, null));
    assetTypeTwo = assetTypeRepository.save(new AssetType("second asset type", "second asset Type description", "sat", "blue", language, null));
    lifecycleStatus = statusRepository.save(new Status("lifecycle status", "lifecycle description", language, null));
    stewardshipStatus = statusRepository.save(new Status("stewardship status", "stewardship description", language, null));
    statusWithNoAssignment = statusRepository.save(new Status("status with no assignment", "stewardship description", language, null));

    lifecycleAssignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetTypeOne, AssignmentStatusType.LIFECYCLE, lifecycleStatus, null));
    stewardshipAssignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetTypeTwo, AssignmentStatusType.STEWARDSHIP, stewardshipStatus, null));
  }

  @AfterEach
  public void clearData () {
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    relationTypeComponentAttributeTypeAssignmentRepository.deleteAll();
    relationTypeAttributeTypeAssignmentRepository.deleteAll();
    attributeHistoryRepository.deleteAll();
    relationAttributeRepository.deleteAll();
    relationComponentAttributesHistoryRepository.deleteAll();
    assetHistoryRepository.deleteAll();
    subscriptionRepository.deleteAll();
    assetLinkUsageRepository.deleteAll();
    customViewRepository.deleteAll();
    assetHierarchyRepository.deleteAll();
    attributeRepository.deleteAll();
    attributeTypeRepository.deleteAll();

    relationRepository.deleteAll();
    relationComponentRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    responsibilityRepository.deleteAll();
    roleRepository.deleteAll();
    assetRepository.deleteAll();

    assetTypeStatusAssignmentRepository.deleteAll();
    assetTypeRepository.deleteAll();
    statusRepository.deleteAll();
  }


  @Test
  public void createAsset_Success_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), null, null);

    PostAssetResponse response = assetsService.createAsset(request, user);

    assertAll(
      () -> assertEquals(request.getAsset_type_id(), response.getAsset_type_id().toString()),
      () -> assertEquals(user.getUserId(), response.getCreated_by()),
      () -> assertNull(request.getLifecycle_status())
    );
  }

  @Test
  public void createAsset_CheckAssetHistory_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), null, null);

    PostAssetResponse response = assetsService.createAsset(request, user);
    List<AssetHistory> assetHistories = assetHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(1, assetHistories.size(), "asset history size"),
      () -> assertEquals("3000-01-01 00:00:00.0", assetHistories.get(0).getValidTo().toString(), "asset history valid to"),
      () -> assertEquals(response.getCreated_on(), assetHistories.get(0).getValidFrom(), "asset history valid from")
    );
  }

  @Test
  public void createAsset_AssetNameDoesNotMatchPattern_IntegrationTest () {
    assetTypeOne.setAssetNameValidationMask("^Hello");
    assetTypeRepository.save(assetTypeOne);
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), null, null);

    assertThrows(AssetNameDoesNotMatchPatternException.class, () -> assetsService.createAsset(request, user));

    assetTypeOne.setAssetNameValidationMask(".*some name.*");
    assetTypeRepository.save(assetTypeOne);
    assertDoesNotThrow(() -> assetsService.createAsset(request, user));
  }

  @Test
  public void createAsset_StatusNotFoundException_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), new UUID(123, 123).toString(), null);

    assertThrows(AssetTypeStatusAssignmentNotFoundException.class, () -> assetsService.createAsset(request, user));
  }

  @Test
  public void createAsset_IllegalArguments_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", "123", new UUID(123, 123).toString(), null);

    assertThrows(IllegalArgumentException.class, () -> assetsService.createAsset(request, user));
  }

  @Test
  public void createAsset_AssetTypeNotFoundException_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", new UUID(123, 123).toString(), new UUID(123, 123).toString(), null);

    assertThrows(AssetTypeNotFoundException.class, () -> assetsService.createAsset(request, user));
  }

  @Test
  public void createAsset_AssetAlreadyExistsException_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), null, null);
    assetRepository.save(new Asset(request.getAsset_name(), assetTypeTwo, "some name", language, null, null, user));

    assertThrows(DataIntegrityViolationException.class, () -> assetsService.createAsset(request, user));
  }

  @Test
  public void createAsset_AssignmentNotFoundException_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), stewardshipStatus.getStatusId().toString(), null);

    assertThrows(AssetTypeStatusAssignmentNotFoundException.class, () -> assetsService.createAsset(request, user));
  }

  @Test
  public void createAssetsBulk_EmptyRequiredFields_IntegrationTest () {
    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest(null, "this will be shown", assetTypeOne.getAssetTypeId().toString(), stewardshipStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", null, assetTypeOne.getAssetTypeId().toString(), stewardshipStatus.getStatusId().toString(), null);
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> assetsService.createAssetsBulk(requests, user));
  }

  @Test
  public void createAssetsBulk_InvalidFieldLengthException_IntegrationTest () {
    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest(StringUtils.repeat("*", 256), "this will be shown", assetTypeOne.getAssetTypeId().toString(), stewardshipStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeOne.getAssetTypeId().toString(), stewardshipStatus.getStatusId().toString(), null);
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(InvalidFieldLengthException.class, () -> assetsService.createAssetsBulk(requests, user));
  }

  @Test
  public void createAssetsBulk_IllegalArgumentsException_IntegrationTest () {
    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest("first asset", "this will be shown", "123", stewardshipStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeOne.getAssetTypeId().toString(), "43243", null);
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(IllegalArgumentException.class, () -> assetsService.createAssetsBulk(requests, user));
  }

  @Test
  public void createAssetsBulk_AssetTypeNotFoundException_IntegrationTest () {
    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest("first asset", "this will be shown", UUID.randomUUID().toString(), stewardshipStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeOne.getAssetTypeId().toString(), stewardshipStatus.getStatusId().toString(), null);
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(AssetTypeNotFoundException.class, () -> assetsService.createAssetsBulk(requests, user));
  }

  @Test
  public void createAssetsBulk_AssetTypeStatusAssignmentNotFoundException_IntegrationTest () {
    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest("first asset", "this will be shown", assetTypeOne.getAssetTypeId().toString(), statusWithNoAssignment.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeTwo.getAssetTypeId().toString(), stewardshipStatus.getStatusId().toString(), null);
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(AssetTypeStatusAssignmentNotFoundException.class, () -> assetsService.createAssetsBulk(requests, user));
  }

  @Test
  public void createAssetsBulk_Success_IntegrationTest () {
    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest("first asset", "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeTwo.getAssetTypeId().toString(), null, stewardshipStatus.getStatusId().toString());
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertAll(
      () -> assertDoesNotThrow(() -> assetsService.createAssetsBulk(requests, user)),
      () -> assertEquals(2, assetRepository.findAll().size())
    );
  }

  @Test
  public void createAssetsBulk_AssetHistory_IntegrationTest () {
    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest("first asset", "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeTwo.getAssetTypeId().toString(), null, stewardshipStatus.getStatusId().toString());
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    List<PostAssetResponse> response = assetsService.createAssetsBulk(requests, user);

    List<AssetHistory> assetHistories = assetHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, assetHistories.size(), "asset history size"),
      () -> assertEquals("3000-01-01 00:00:00.0", assetHistories.get(0).getValidTo().toString(), "0 asset history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", assetHistories.get(1).getValidTo().toString(), "1 asset history valid to"),
      () -> assertEquals(response.get(0).getCreated_on(), assetHistories.stream().filter(asset -> asset.getAssetId().equals(response.get(0).getAsset_id())).findFirst().get().getValidFrom(), "0 asset history valid from"),
      () -> assertEquals(response.get(1).getCreated_on(), assetHistories.stream().filter(asset -> asset.getAssetId().equals(response.get(1).getAsset_id())).findFirst().get().getValidFrom(), "1 asset history valid from")
    );
  }

  @Test
  public void createAssetsBulk_AssetNameDoesNotMatchPatternException_IntegrationTest () {
    assetTypeOne.setAssetNameValidationMask("^Hello");
    assetTypeRepository.save(assetTypeOne);

    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest("first asset", "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeTwo.getAssetTypeId().toString(), null, stewardshipStatus.getStatusId().toString());
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(AssetNameDoesNotMatchPatternException.class, () -> assetsService.createAssetsBulk(requests, user));

    assetTypeOne.setAssetNameValidationMask("^first.*");
    assetTypeRepository.save(assetTypeOne);

    assetTypeTwo.setAssetNameValidationMask("^some.*");
    assetTypeRepository.save(assetTypeTwo);

    assertDoesNotThrow(() -> assetsService.createAssetsBulk(requests, user));
  }

  @Test
  public void updateAsset_Success_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest(null, "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId().toString(), null);
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));

    AssetResponse response = assetsService.updateAsset(asset.getAssetId(), request, user);

    assertAll(
      () -> assertEquals(asset.getAssetName(), response.getAsset_name()),
      () -> assertEquals(request.getAsset_type_id(), response.getAsset_type_id().toString()),
      () -> assertNotNull(response.getLifecycle_status()),
      () -> assertNotEquals(asset.getLastModifiedOn(), response.getLast_modified_on()),
      () -> assertNotNull(response.getLast_modified_by())
    );
  }

  @Test
  public void updateAsset_AssetHistory_IntegrationTest () {
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), null, null);
    PostAssetResponse postResponse = assetsService.createAsset(postRequest, user);

    PostOrPatchAssetRequest patchRequest = new PostOrPatchAssetRequest(null, "new this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId().toString(), null);
    AssetResponse patchResponse = assetsService.updateAsset(postResponse.getAsset_id(), patchRequest, user);

    List<AssetHistory> assetHistories = assetHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, assetHistories.size(), "asset history size"),
      () -> assertEquals(postResponse.getCreated_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.getAsset_displayname())).findFirst().get().getValidFrom(), "post asset history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(patchResponse.getAsset_displayname())).findFirst().get().getValidFrom(), "patch asset history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.getAsset_displayname())).findFirst().get().getValidTo(), "post asset history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(patchResponse.getAsset_displayname())).findFirst().get().getValidTo().toString(), "patched asset history valid to")
    );
  }

  @Test
  public void updateAsset_AssetNameDoesNotMatchPatternException_IntegrationTest () {
    assetTypeOne.setAssetNameValidationMask("^Hello");
    assetTypeRepository.save(assetTypeOne);

    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("new name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId().toString(), null);
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));

    assertThrows(AssetNameDoesNotMatchPatternException.class, () -> assetsService.updateAsset(asset.getAssetId(), request, user));

    assetTypeOne.setAssetNameValidationMask("^new.*");
    assetTypeRepository.save(assetTypeOne);

    assertDoesNotThrow(() -> assetsService.updateAsset(asset.getAssetId(), request, user));
  }

  @Test
  public void updateAsset_IllegalArgumentException_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest(null, "this will be shown", "123", lifecycleStatus.getStatusId().toString(), null);
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));

    assertThrows(IllegalArgumentException.class, () -> assetsService.updateAsset(asset.getAssetId(), request, user));
  }

  @Test
  public void updateAsset_AssetTypeStatusAssignmentNotFoundException_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest(null, "this will be shown", assetTypeOne.getAssetTypeId().toString(), stewardshipStatus.getStatusId().toString(), null);
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));

    assertThrows(AssetTypeStatusAssignmentNotFoundException.class, () -> assetsService.updateAsset(asset.getAssetId(), request, user));
  }

  @Test
  public void updateAsset_AssetTypeNotFoundException_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest(null, "this will be shown", new UUID(123, 123).toString(), lifecycleStatus.getStatusId().toString(), null);
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));

    assertThrows(AssetTypeNotFoundException.class, () -> assetsService.updateAsset(asset.getAssetId(), request, user));
  }

  @Test
  public void updateAsset_AssetAlreadyExists_IntegrationTest () {
    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("another name", "this will be shown", null, lifecycleStatus.getStatusId().toString(), null);
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    assetRepository.save(new Asset("another name", assetTypeTwo, "displayed name", language, null, null, user));

    assertThrows(DataIntegrityViolationException.class, () -> assetsService.updateAsset(asset.getAssetId(), request, user));
  }

  @Test
  public void updateAssetBulk_Success_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(asset.getAssetId(), null, "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), null, "second this will be shown", null, null, stewardshipStatus.getStatusId());

    List<AssetResponse> responses = assetsService.updateBulkAsset(List.of(
      firstRequest,
      secondRequest
    ), user);

    assertAll(
      () -> assertEquals(asset.getAssetName(), responses.get(0).getAsset_name()),
      () -> assertEquals(firstRequest.getAsset_type_id(), responses.get(0).getAsset_type_id().toString()),
      () -> assertNotNull(firstRequest.getLifecycle_status()),
      () -> assertNotEquals(asset.getLastModifiedOn(), responses.get(0).getLast_modified_on()),
      () -> assertNotNull(responses.get(0).getLast_modified_by()),
      () -> assertEquals(secondAsset.getAssetName(), responses.get(1).getAsset_name()),
      () -> assertNotNull(responses.get(1).getStewardship_status()),
      () -> assertNotEquals(secondAsset.getLastModifiedOn(), responses.get(1).getLast_modified_on()),
      () -> assertNotNull(responses.get(1).getLast_modified_by())
    );
  }

  @Test
  public void updateAssetBulk_AssetHistory_IntegrationTest () {
    PostOrPatchAssetRequest firstPostRequest = new PostOrPatchAssetRequest("first asset", "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondPostRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeTwo.getAssetTypeId().toString(), null, stewardshipStatus.getStatusId().toString());
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstPostRequest);
    requests.add(secondPostRequest);

    List<PostAssetResponse> postResponse = assetsService.createAssetsBulk(requests, user);

    PatchAssetRequest firstRequest = new PatchAssetRequest(postResponse.get(0).getAsset_id(), null, "new this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(postResponse.get(1).getAsset_id(), null, "second this will be shown", null, null, stewardshipStatus.getStatusId());

    List<AssetResponse> patchResponses = assetsService.updateBulkAsset(List.of(
      firstRequest,
      secondRequest
    ), user);

    List<AssetHistory> assetHistories = assetHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(4, assetHistories.size(), "asset history size"),
      () -> assertEquals(postResponse.get(0).getCreated_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.get(0).getAsset_displayname())).findFirst().get().getValidFrom(), "first post asset history valid from"),
      () -> assertEquals(postResponse.get(1).getCreated_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.get(1).getAsset_displayname())).findFirst().get().getValidFrom(), "second post asset history valid from"),
      () -> assertEquals(patchResponses.get(0).getLast_modified_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(patchResponses.get(0).getAsset_displayname())).findFirst().get().getValidFrom(), "first patch asset history valid from"),
      () -> assertEquals(patchResponses.get(1).getLast_modified_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(patchResponses.get(1).getAsset_displayname())).findFirst().get().getValidFrom(), "second patch asset history valid from"),
      () -> assertEquals(patchResponses.get(0).getLast_modified_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.get(0).getAsset_displayname())).findFirst().get().getValidTo(), "first post asset history valid to"),
      () -> assertEquals(patchResponses.get(1).getLast_modified_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.get(1).getAsset_displayname())).findFirst().get().getValidTo(), "second post asset history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(patchResponses.get(0).getAsset_displayname())).findFirst().get().getValidTo().toString(), "first patched asset history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(patchResponses.get(1).getAsset_displayname())).findFirst().get().getValidTo().toString(), "second patched asset history valid to")
    );
  }

  @Test
  public void updateAssetBulk_AssetNameDoesNotMatchPatternException_IntegrationTest () {
    assetTypeOne.setAssetNameValidationMask("^Hello");
    assetTypeRepository.save(assetTypeOne);

    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(asset.getAssetId(), "second name to", "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), "second to", "second this will be shown", null, null, stewardshipStatus.getStatusId());

    assertThrows(AssetNameDoesNotMatchPatternException.class, () -> assetsService.updateBulkAsset(List.of(
      firstRequest,
      secondRequest
    ), user));

    assetTypeOne.setAssetNameValidationMask("^second.*");
    assetTypeRepository.save(assetTypeOne);

    assertDoesNotThrow(() -> assetsService.updateBulkAsset(List.of(
      firstRequest,
      secondRequest
    ), user));
  }

  @Test
  public void updateAssetBulk_SomeRequiredFieldsAreEmptyException_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(null, null, "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), null, "second this will be shown", null, null, stewardshipStatus.getStatusId());

    assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> assetsService.updateBulkAsset(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void updateAssetBulk_InvalidFieldLengthException_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(asset.getAssetId(), StringUtils.repeat("*", 256), null, assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), null, "second this will be shown", null, null, stewardshipStatus.getStatusId());

    assertThrows(InvalidFieldLengthException.class, () -> assetsService.updateBulkAsset(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void updateAssetBulk_DuplicateValueInRequestException_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(asset.getAssetId(), "new name", null, assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), "new name", "second this will be shown", null, null, stewardshipStatus.getStatusId());

    assertThrows(DuplicateValueInRequestException.class, () -> assetsService.updateBulkAsset(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void updateAssetBulk_AssetNotFoundException_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(UUID.randomUUID(), "some new name", null, assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), null, "second this will be shown", null, null, stewardshipStatus.getStatusId());

    assertThrows(AssetNotFoundException.class, () -> assetsService.updateBulkAsset(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void updateAssetBulk_AssetTypeNotFoundException_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(asset.getAssetId(), "some new name", null, UUID.randomUUID().toString(), lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), null, "second this will be shown", null, null, stewardshipStatus.getStatusId());

    assertThrows(AssetTypeNotFoundException.class, () -> assetsService.updateBulkAsset(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void updateAssetBulk_AssetTypeStatusAssignmentNotFoundException_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(asset.getAssetId(), null, "this will be shown", assetTypeOne.getAssetTypeId().toString(), stewardshipStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), null, "second this will be shown", null, null, stewardshipStatus.getStatusId());

    assertThrows(AssetTypeStatusAssignmentNotFoundException.class, () -> assetsService.updateBulkAsset(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void updateAssetBulk_AssetNameAlreadyExistsException_IntegrationTest () {
    assetRepository.save(new Asset("another name", assetTypeTwo, "displayed name", language, null, null, user));

    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name was", assetTypeOne, "displayed name", language, null, null, user));
    PatchAssetRequest firstRequest = new PatchAssetRequest(asset.getAssetId(), "another name", null, null, lifecycleStatus.getStatusId(), null);
    PatchAssetRequest secondRequest = new PatchAssetRequest(secondAsset.getAssetId(), null, "second this will be shown", null, null, stewardshipStatus.getStatusId());

    assertThrows(AssetNameAlreadyExistsException.class, () -> assetsService.updateBulkAsset(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void getAssetById_Success_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("name was", assetTypeTwo, "displayed name", language, lifecycleStatus, null, user));

    AssetResponse response = assetsService.getAssetById(asset.getAssetId());

    assertAll(
      () -> assertEquals(asset.getAssetName(), response.getAsset_name()),
      () -> assertEquals(asset.getLifecycleStatus().getStatusId(), response.getLifecycle_status())
    );
  }

  @Test
  public void getAssetById_AssetNotFoundException_IntegrationTest () {
    assertThrows(AssetNotFoundException.class, () -> assetsService.getAssetById(new UUID(123, 123)));
  }

  @Test
  public void getAssetsByParams_Success_IntegrationTest () {
    Asset assetWithNoStatus = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, statusWithNoAssignment, null, user));
    Asset assetWithLifecycleStatus = assetRepository.save(new Asset("lifecycle asset", assetTypeOne, "life asset name", language, lifecycleStatus, statusWithNoAssignment, user));
    Asset assetWithStewardshipStatus = assetRepository.save(new Asset("stewardship asset", assetTypeTwo, "stewardship name", language, null, stewardshipStatus, user));
    Asset assetWithBothStatuses = assetRepository.save(new Asset("has both statuses", assetTypeTwo, "stewardship name", language, lifecycleStatus, stewardshipStatus, user));
    assetHierarchyRepository.save(new AssetHierarchy(assetWithNoStatus, assetWithBothStatuses, null, user));

    assertAll(
      () -> assertEquals(4, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "all assets with root flag = false"),
      () -> assertEquals(1, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, List.of(assetTypeOne.getAssetTypeId()), null, null, false, null, null, 0, 50)).getResults().size(), "asset type one"),
      () -> assertEquals(4, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, List.of(assetTypeOne.getAssetTypeId(), assetTypeTwo.getAssetTypeId()), null, null, false, null, null, 0, 50)).getResults().size(), "asset type one and asset type two"),
      () -> assertEquals(3, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, List.of(assetTypeTwo.getAssetTypeId()), null, null, false, null, null, 0, 50)).getResults().size(), "asset type two"),
      () -> assertEquals(1, assetsService.getAssetsByParams(new GetAssetParams("asset", null, null, false, List.of(assetTypeTwo.getAssetTypeId()), null, null, false, null, null, 0, 50)).getResults().size(), "asset name and asset type two"),
      () -> assertEquals(2, assetsService.getAssetsByParams(new GetAssetParams("asset", null, null, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "asset name"),
      () -> assertEquals(2, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, null, List.of(lifecycleStatus.getStatusId()), null, false, null, null, 0, 50)).getResults().size(), "lifecycle status"),
      () -> assertEquals(3, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, null, List.of(lifecycleStatus.getStatusId(), statusWithNoAssignment.getStatusId()), null, false, null, null, 0, 50)).getResults().size(), "lifecycle status and status with not assignment"),
      () -> assertEquals(1, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, null, List.of(lifecycleStatus.getStatusId()), List.of(stewardshipStatus.getStatusId()), false, null, null, 0, 50)).getResults().size(), "lifecycle status and stewardship status"),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, null, List.of(stewardshipStatus.getStatusId()), null, false, null, null, 0, 50)).getResults().size(), "stewardship status"),
      () -> assertEquals(3, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, false, null, null, List.of(stewardshipStatus.getStatusId(), statusWithNoAssignment.getStatusId()), false, null, null, 0, 50)).getResults().size(), "stewardship status and status with not assignment"),
      () -> assertEquals(1, assetsService.getAssetsByParams(new GetAssetParams("CYcLe", null, null, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by \"CYcLe\""),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams("ShIp", null, null, false, null, List.of(lifecycleStatus.getStatusId()), null, false, null, null, 0, 50)).getResults().size(), "search by \"ShIp\""),
      () -> assertEquals(2, assetsService.getAssetsByParams(new GetAssetParams("asset", null, AssetSearchMode.ANY, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset name \"asset\" ANY search mode"),
      () -> assertEquals(2, assetsService.getAssetsByParams(new GetAssetParams("asset", null, AssetSearchMode.RIGHT, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset name \"asset\" RIGHT search mode"),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams("asset", null, AssetSearchMode.LEFT, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset name \"asset\" LEFT search mode"),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams("asset", null, AssetSearchMode.EXACT_MATCH, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset name \"asset\" EXACT search mode"),
      () -> assertEquals(4, assetsService.getAssetsByParams(new GetAssetParams(null, "name", AssetSearchMode.ANY, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset displayName \"name\" ANY search mode"),
      () -> assertEquals(4, assetsService.getAssetsByParams(new GetAssetParams(null, "name", AssetSearchMode.RIGHT, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset displayName \"name\" RIGHT search mode"),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams(null, "name", AssetSearchMode.LEFT, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset displayName \"name\" LEFT search mode"),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams(null, "name", AssetSearchMode.EXACT_MATCH, false, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset displayName \"name\" EXACT search mode"),
      () -> assertEquals(4, assetsService.getAssetsByParams(new GetAssetParams("asset", "name", AssetSearchMode.ANY, true, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset name \"asset\" and asset displayName \"name\" ANY search mode"),
      () -> assertEquals(4, assetsService.getAssetsByParams(new GetAssetParams("asset", "name", AssetSearchMode.RIGHT, true, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset name \"asset\" and asset displayName \"name\" RIGHT search mode"),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams("asset", "name", AssetSearchMode.LEFT, true, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset name \"asset\" and asset displayName \"name\" LEFT search mode"),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams("asset", "name", AssetSearchMode.EXACT_MATCH, true, null, null, null, false, null, null, 0, 50)).getResults().size(), "search by asset name \"asset\" and asset displayName \"name\" EXACT search mode"),
      () -> assertEquals(3, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, null, null, null, null, true, null, null, 0, 50)).getResults().size(), "search by root flag"),
      () -> assertEquals(4, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, null, null, null, null, null, null, null, 0, 50)).getResults().size(), "search by all")
    );
  }

  @Test
  public void getAssetsByParams_Pagination_IntegrationTest () {
    generateAssets(120);

    assertAll(
      () -> assertEquals(2, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, null, null, null, null, false, null, null, 0, 2)).getResults().size()),
      () -> assertEquals(50, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, null, null, null, null, false, null, null, 0, 50)).getResults().size()),
      () -> assertEquals(0, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, null, null, null, null, false, null, null, 3, 50)).getResults().size()),
      () -> assertEquals(100, assetsService.getAssetsByParams(new GetAssetParams(null, null,  null,null, null, null, null, false, null, null, 0, 130)).getResults().size()),
      () -> assertEquals(120, assetsService.getAssetsByParams(new GetAssetParams(null, null, null, null, null, null, null, false, null, null, 0, 50)).getTotal()),
      () -> assertEquals(11, assetsService.getAssetsByParams(new GetAssetParams("11", null, null, false, null, null, null, false, null, null, 0, 50)).getTotal())
    );
  }

  @Test
  public void getAssetsChildren_Success_IntegrationTest () {
    // 1. Create relation_type and relation_type_component
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first relation type", "1", 2, false, true, language, null));
    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second relation type", "2", 2, false, true, language, null));
    RelationType thirdRelationType = relationTypeRepository.save(new RelationType("third relation type", "3", 2, false, true, language, null));
    RelationType forthRelationType = relationTypeRepository.save(new RelationType("forth relation type", "4", 2, false, true, language, null));

    // 2. Create asset type
    AssetType firstAssetType = assetTypeRepository.save(new AssetType("1 first asset type", "1", "1", "red", language, null));
    AssetType secondAssetType = assetTypeRepository.save(new AssetType("2 second asset type", "2", "2", "red", language, null));
    AssetType thirdAssetType = assetTypeRepository.save(new AssetType("3 third asset type", "3", "3", "red", language, null));
    AssetType forthAssetType = assetTypeRepository.save(new AssetType("4 forth asset type", "4", "4", "red", language, null));
    AssetType fifthAssetType = assetTypeRepository.save(new AssetType("5 fifth asset type", "5", "5", "red", language, null));

    // 3. Create Status
    Status firstStatus = statusRepository.save(new Status("first status", "1", language, null));
    Status secondStatus = statusRepository.save(new Status("second status", "2", language, null));
    Status thirdStatus = statusRepository.save(new Status("third status", "3", language, null));

    // 4. Create assets
    Asset firstAsset = assetRepository.save(new Asset("first asset name", firstAssetType, "1", language, firstStatus, secondStatus, null));
    Asset secondAsset = assetRepository.save(new Asset("second asset name", secondAssetType, "2", language, null, secondStatus, null));
    Asset thirdAsset = assetRepository.save(new Asset("third asset name", thirdAssetType, "3", language, thirdStatus, secondStatus, null));
    Asset forthAsset = assetRepository.save(new Asset("forth asset name", forthAssetType, "4", language, thirdStatus, firstStatus, null));
    Asset fifthAsset = assetRepository.save(new Asset("fifth asset name", fifthAssetType, "5", language, null, thirdStatus, null));

    // 5. Create relation and relation component
    Relation firstRelation = relationRepository.save(new Relation(firstRelationType, null));
    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, null));
    Relation thirdRelation = relationRepository.save(new Relation(thirdRelationType, null));
    Relation forthRelation = relationRepository.save(new Relation(forthRelationType, null));

    // 6. Asset hierarchy
    // 1 -> 2
    // 2 -> 3
    // 3 -> 4
    // 4 -> 5
    assetHierarchyRepository.save(new AssetHierarchy(firstAsset, secondAsset, firstRelation, null));
    assetHierarchyRepository.save(new AssetHierarchy(secondAsset, thirdAsset, secondRelation, null));
    assetHierarchyRepository.save(new AssetHierarchy(thirdAsset, forthAsset, thirdRelation, null));
    assetHierarchyRepository.save(new AssetHierarchy(forthAsset, fifthAsset, forthRelation, null));

    // 7. Create Attributes
    Attribute firstAttribute = new Attribute(null, firstAsset, language, null);
    firstAttribute.setValue("1");
    attributeRepository.save(firstAttribute);
    Attribute secondAttributeForSecondAsset = new Attribute(null, secondAsset, language, null);
    secondAttributeForSecondAsset.setValue("2.2");
    attributeRepository.save(secondAttributeForSecondAsset);

    Attribute secondAttributeForThirdAsset = new Attribute(null, thirdAsset, language, null);
    secondAttributeForThirdAsset.setValue("2.3");
    attributeRepository.save(secondAttributeForThirdAsset);

    Attribute thirdAttribute = new Attribute(null, thirdAsset, language, null);
    thirdAttribute.setValue("3");
    attributeRepository.save(thirdAttribute);
    Attribute forthAttributeForForthAsset = new Attribute(null, forthAsset, language, null);
    forthAttributeForForthAsset.setValue("4.4");
    attributeRepository.save(forthAttributeForForthAsset);

    Attribute forthAttributeForSecondAsset = new Attribute(null, secondAsset, language, null);
    forthAttributeForSecondAsset.setValue("4.2");
    attributeRepository.save(forthAttributeForSecondAsset);

    Attribute forthAttributeForThirdAsset = new Attribute(null, thirdAsset, language, null);
    forthAttributeForThirdAsset.setValue("4.3");
    attributeRepository.save(forthAttributeForThirdAsset);

    Attribute fifthAttribute = new Attribute(null, fifthAsset, language, null);
    fifthAttribute.setValue("5");
    attributeRepository.save(fifthAttribute);

    assertAll(
      () -> assertEquals(2, assetsService.getAssetsChildren(firstAsset.getAssetId(), null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, assetsService.getAssetsChildren(secondAsset.getAssetId(), null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, assetsService.getAssetsChildren(thirdAsset.getAssetId(), null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, assetsService.getAssetsChildren(thirdAsset.getAssetId(), null, null, null, null, null, null, 0, 50).getResults().get(0).getChildren_asset_count()),
      () -> assertEquals(1, assetsService.getAssetsChildren(forthAsset.getAssetId(), null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, assetsService.getAssetsChildren(fifthAsset.getAssetId(), null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, assetsService.getAssetsChildren(fifthAsset.getAssetId(), null, null, List.of(secondStatus.getStatusId()), null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(2, assetsService.getAssetsChildren(firstAsset.getAssetId(), null, null, null, List.of(secondStatus.getStatusId()), null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, assetsService.getAssetsChildren(thirdAsset.getAssetId(), null, null, List.of(thirdStatus.getStatusId()), List.of(firstStatus.getStatusId()), null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, assetsService.getAssetsChildren(thirdAsset.getAssetId(), null, null, List.of(thirdStatus.getStatusId()), List.of(secondStatus.getStatusId()), null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, assetsService.getAssetsChildren(thirdAsset.getAssetId(), null, null, null, List.of(firstStatus.getStatusId()), null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, assetsService.getAssetsChildren(forthAsset.getAssetId(), null, null, null, List.of(thirdStatus.getStatusId()), null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, assetsService.getAssetsChildren(firstAsset.getAssetId(), null, List.of(firstAssetType.getAssetTypeId()), null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(2, assetsService.getAssetsChildren(firstAsset.getAssetId(), null, List.of(secondAssetType.getAssetTypeId()), null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, assetsService.getAssetsChildren(secondAsset.getAssetId(), null, List.of(thirdAssetType.getAssetTypeId()), null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, assetsService.getAssetsChildren(secondAsset.getAssetId(), null, List.of(secondAssetType.getAssetTypeId()), null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, assetsService.getAssetsChildren(secondAsset.getAssetId(), "3", null, null, null, null, null, 0, 50).getResults().size(), "Second asset display name 3"),
      () -> assertEquals(0, assetsService.getAssetsChildren(secondAsset.getAssetId(), "2", null, null, null, null, null, 0, 50).getResults().size(), "Second asset display name 2"),
      () -> assertEquals(2, assetsService.getAssetsChildren(firstAsset.getAssetId(), "2", null, null, null, null, null, 0, 50).getResults().size(), "First asset display name 2")
    );
  }

  @Test
  public void getAssetPathElements_Success_IntegrationTest () {
    // 1. Create assets
    Asset firstAsset = assetRepository.save(new Asset("first asset name", null, "1", language, null, null, null));
    Asset secondAsset = assetRepository.save(new Asset("second asset name", null, "2", language, null, null, null));
    Asset thirdAsset = assetRepository.save(new Asset("third asset name", null, "3", language, null, null, null));
    Asset forthAsset = assetRepository.save(new Asset("forth asset name", null, "4", language, null, null, null));
    Asset fifthAsset = assetRepository.save(new Asset("fifth asset name", null, "5", language, null, null, null));

    // 2. Create relation and relation component
    Relation firstRelation = relationRepository.save(new Relation());
    Relation secondRelation = relationRepository.save(new Relation());
    Relation thirdRelation = relationRepository.save(new Relation());
    Relation forthRelation = relationRepository.save(new Relation());

    // 3. Asset hierarchy
    // 1 -> 2
    // 2 -> 3
    // 3 -> 4
    // 4 -> 5
    assetHierarchyRepository.save(new AssetHierarchy(firstAsset, secondAsset, firstRelation, null));
    assetHierarchyRepository.save(new AssetHierarchy(secondAsset, thirdAsset, secondRelation, null));
    assetHierarchyRepository.save(new AssetHierarchy(thirdAsset, forthAsset, thirdRelation, null));
    assetHierarchyRepository.save(new AssetHierarchy(forthAsset, fifthAsset, forthRelation, null));

    // assetRepository.getAssetChain(thirdAsset.getAssetId())
    assertAll(
      () -> assertEquals("1", assetsService.getAssetPath(firstAsset.getAssetId()).getPath()),
      () -> assertEquals("1>2", assetsService.getAssetPath(secondAsset.getAssetId()).getPath()),
      () -> assertEquals("1>2>3", assetsService.getAssetPath(thirdAsset.getAssetId()).getPath()),
      () -> assertEquals("1>2>3>4", assetsService.getAssetPath(forthAsset.getAssetId()).getPath()),
      () -> assertEquals("1>2>3>4>5", assetsService.getAssetPath(fifthAsset.getAssetId()).getPath())
    );
  }

  @Test
  public void getAssetRelationTypesIntegrationTest () {
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first relation type name", "description", 2, false, false, language, user));
    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second relation type name", "description", 2, false, false, language, user));
    RelationType thirdRelationType = relationTypeRepository.save(new RelationType("third relation type name", "description", 2, false, false, language, user));

    RelationTypeComponent firstFirstRelationType = relationTypeComponentRepository.save(new RelationTypeComponent("first first relation type component", "desc", null, null, null, language, firstRelationType, user));
    RelationTypeComponent firstSecondRelationType = relationTypeComponentRepository.save(new RelationTypeComponent("first second relation type component", "desc", null, null, null, language, firstRelationType, user));

    RelationTypeComponent secondFirstRelationType = relationTypeComponentRepository.save(new RelationTypeComponent("second first relation type component", "desc", null, null, null, language, secondRelationType, user));
    RelationTypeComponent secondSecondRelationType = relationTypeComponentRepository.save(new RelationTypeComponent("second second relation type component", "desc", null, null, null, language, secondRelationType, user));

    RelationTypeComponent thirdFirstRelationType = relationTypeComponentRepository.save(new RelationTypeComponent("third first relation type component", "desc", null, null, null, language, thirdRelationType, user));
    RelationTypeComponent thirdSecondRelationType = relationTypeComponentRepository.save(new RelationTypeComponent("third second relation type component", "desc", null, null, null, language, thirdRelationType, user));

    Relation firstFirstRelation = relationRepository.save(new Relation(firstRelationType, user));
    Relation firstSecondRelation = relationRepository.save(new Relation(firstRelationType, user));
    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    Relation thirdRelation = relationRepository.save(new Relation(thirdRelationType, user));

    Asset asset = assetRepository.save(new Asset("first asset", null, "asc", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second asset", null, "asc", language, null, null, user));

    relationComponentRepository.save(new RelationComponent(firstFirstRelation, firstFirstRelationType, asset, null, null, user));
    relationComponentRepository.save(new RelationComponent(firstSecondRelation, firstFirstRelationType, secondAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondSecondRelationType, asset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, firstSecondRelationType, secondAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(thirdRelation, thirdFirstRelationType, secondAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(thirdRelation, thirdSecondRelationType, secondAsset, null, null, user));

    assertAll(
      () -> assertEquals(2, assetsService.getAssetRelationTypes(asset.getAssetId()).getTotal()),
      () -> assertEquals("first relation type name", assetsService.getAssetRelationTypes(asset.getAssetId()).getResults().stream().filter(relationType -> relationType.getRelation_type_name().equals("first relation type name")).findFirst().get().getRelation_type_name()),
      () -> assertEquals("second relation type name", assetsService.getAssetRelationTypes(asset.getAssetId()).getResults().stream().filter(relationType -> relationType.getRelation_type_name().equals("second relation type name")).findFirst().get().getRelation_type_name()),
      () -> assertEquals(2, assetsService.getAssetRelationTypes(asset.getAssetId()).getResults().stream().filter(relationType -> relationType.getRelation_type_component().size() == 1).toList().size()),
      () -> assertEquals(0, assetsService.getAssetRelationTypes(asset.getAssetId()).getResults().stream().filter(relationType -> relationType.getRelation_type_component().size() == 2).toList().size())
    );
  }

  @Test
  public void getAssetAttributeLinkUsage_Success_IntegrationTest () {
    AssetType firstAssetType = assetTypeRepository.save(new AssetType("1 first asset type", "desc", "acr", "color", language, user));
    AssetType secondAssetType = assetTypeRepository.save(new AssetType("2 second asset type", "desc", "acr", "color", language, user));
    AssetType thirdAssetType = assetTypeRepository.save(new AssetType("3 third asset type", "desc", "acr", "color", language, user));

    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType("first attribute name", "desc", AttributeKindType.TEXT, null, null, language, user));
    AttributeType secondAttributeType = attributeTypeRepository.save(new AttributeType("second attribute name", "desc", AttributeKindType.TEXT, null, null, language, user));
    AttributeType thirdAttributeType = attributeTypeRepository.save(new AttributeType("third attribute name", "desc", AttributeKindType.TEXT, null, null, language, user));

    Status firstStatusStewardship = statusRepository.save(new Status("first stewardship status name", "desc", language, user));
    Status secondStatusStewardship = statusRepository.save(new Status("second stewardship status name", "desc", language, user));

    Status firstStatusLifecycle = statusRepository.save(new Status("first lifecycle status name", "desc", language, user));
    Status secondStatusLifecycle = statusRepository.save(new Status("second lifecycle status name", "desc", language, user));

    Asset firstAsset = assetRepository.save(new Asset("first asset", firstAssetType, "asc", language, null, firstStatusStewardship, user));
    Attribute firstAttribute = new Attribute(firstAttributeType, firstAsset, language, user);
    firstAttribute.setValue("1");
    attributeRepository.save(firstAttribute);
    Attribute secondAttribute = new Attribute(secondAttributeType, firstAsset, language, user);
    firstAttribute.setValue("2");
    attributeRepository.save(secondAttribute);
    Attribute thirdAttribute = new Attribute(thirdAttributeType, firstAsset, language, user);
    firstAttribute.setValue("3");
    attributeRepository.save(thirdAttribute);

    Asset secondAsset = assetRepository.save(new Asset("second asset", secondAssetType, "asc", language, null, secondStatusStewardship, user));
    Attribute forthAttribute = new Attribute(firstAttributeType, secondAsset, language, user);
    firstAttribute.setValue("4");
    attributeRepository.save(forthAttribute);

    Asset thirdAsset = assetRepository.save(new Asset("third asset", thirdAssetType, "asc", language, firstStatusLifecycle, secondStatusStewardship, user));
    Attribute fifthAttribute = new Attribute(secondAttributeType, thirdAsset, language, user);
    firstAttribute.setValue("5");
    attributeRepository.save(fifthAttribute);
    Attribute sixthAttribute = new Attribute(thirdAttributeType, thirdAsset, language, user);
    firstAttribute.setValue("6");
    attributeRepository.save(sixthAttribute);

    Asset forthAsset = assetRepository.save(new Asset("forth asset", secondAssetType, "asc", language, firstStatusLifecycle, firstStatusStewardship, user));
    Attribute seventhAttribute = new Attribute(secondAttributeType, forthAsset, language, user);
    firstAttribute.setValue("7");
    attributeRepository.save(seventhAttribute);
    Attribute eigthAttribute = new Attribute(firstAttributeType, forthAsset, language, user);
    firstAttribute.setValue("8");
    attributeRepository.save(eigthAttribute);

    Asset fifthAsset = assetRepository.save(new Asset("fifth asset", thirdAssetType, "asc", language, secondStatusLifecycle, null, user));
    Attribute ninthAttribute = new Attribute(thirdAttributeType, fifthAsset, language, user);
    firstAttribute.setValue("9");
    attributeRepository.save(ninthAttribute);

    AssetLinkUsage firstAssetFirstLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(firstAttribute, firstAsset, user));
    AssetLinkUsage firstAssetSecondLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(fifthAttribute, firstAsset, user));
    AssetLinkUsage secondAssetFirstLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(secondAttribute, secondAsset, user));
    AssetLinkUsage thirdAssetFirstLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(thirdAttribute, thirdAsset, user));
    AssetLinkUsage thirdAssetSecondLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(sixthAttribute, thirdAsset, user));
    AssetLinkUsage forthAssetFirstLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(sixthAttribute, forthAsset, user));
    AssetLinkUsage fifthAssetFirstLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(forthAttribute, fifthAsset, user));
    AssetLinkUsage fifthAssetSecondLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(fifthAttribute, fifthAsset, user));

    assertAll(
      () -> assertEquals(2, assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), null, null, null, null, 0, 50).getResults().size(), "first asset;"),
      () -> assertEquals(0, assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), List.of(secondAssetType.getAssetTypeId()), null, null, null, 0, 50).getResults().size(), "first asset; second asset type"),
      () -> assertEquals(1, assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), List.of(firstAssetType.getAssetTypeId()), List.of(firstAttributeType.getAttributeTypeId()), null, null, 0, 50).getResults().size(), "first asset; first asset type; first attribute type;"),
      () -> assertEquals(0, assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), List.of(firstAssetType.getAssetTypeId()), List.of(secondAttributeType.getAttributeTypeId()), null, null, 0, 50).getResults().size(), "first asset; first asset type; second attribute type;"),
      () -> assertEquals(0, assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), List.of(firstAssetType.getAssetTypeId()), List.of(secondAttributeType.getAttributeTypeId(), thirdAttributeType.getAttributeTypeId()), null, null, 0, 50).getResults().size(), "first asset; first asset type; second attribute type, third attribute type;"),
      () -> assertEquals(1, assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), null, null, null, List.of(firstStatusStewardship.getStatusId()), 0, 50).getResults().size(), "first asset; first status stewardship;"),
      () -> assertEquals(1, assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), null, null, null, List.of(secondStatusStewardship.getStatusId()), 0, 50).getResults().size(), "first asset; second status stewardship;"),
      () -> assertEquals(1, assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), null, null, List.of(firstStatusLifecycle.getStatusId()), null, 0, 50).getResults().size(), "first asset; first status lifecycle;"),
      () -> assertEquals(1, assetsService.getAssetAttributeLinkUsage(secondAsset.getAssetId(), null, null, null, null, 0, 50).getResults().size(), "second asset;"),
      () -> assertEquals(2, assetsService.getAssetAttributeLinkUsage(thirdAsset.getAssetId(), null, null, null, null, 0, 50).getResults().size(), "third asset;"),
      () -> assertEquals(0, assetsService.getAssetAttributeLinkUsage(thirdAsset.getAssetId(), null, List.of(secondAttributeType.getAttributeTypeId()), null, null, 0, 50).getResults().size(), "third asset; second attribute type;"),
      () -> assertEquals(0, assetsService.getAssetAttributeLinkUsage(thirdAsset.getAssetId(), null, List.of(firstAttributeType.getAttributeTypeId()), null, null, 0, 50).getResults().size(), "third asset; first attribute type;"),
      () -> assertEquals(2, assetsService.getAssetAttributeLinkUsage(thirdAsset.getAssetId(), null, List.of(thirdAttributeType.getAttributeTypeId()), null, null, 0, 50).getResults().size(), "third asset; third attribute type;"),
      () -> assertEquals(1, assetsService.getAssetAttributeLinkUsage(forthAsset.getAssetId(), null, null, null, null, 0, 50).getResults().size(), "forth asset;"),
      () -> assertEquals(0, assetsService.getAssetAttributeLinkUsage(forthAsset.getAssetId(), null, null, List.of(secondStatusLifecycle.getStatusId()), null, 0, 50).getResults().size(), "forth asset; second lifecycle status;"),
      () -> assertEquals(1, assetsService.getAssetAttributeLinkUsage(forthAsset.getAssetId(), null, null, List.of(firstStatusLifecycle.getStatusId()), null, 0, 50).getResults().size(), "forth asset; first lifecycle status;"),
      () -> assertEquals(2, assetsService.getAssetAttributeLinkUsage(fifthAsset.getAssetId(), null, null, null, null, 0, 50).getResults().size(), "fifth asset;"),
      () -> assertEquals(firstAsset.getAssetId(), assetsService.getAssetAttributeLinkUsage(firstAsset.getAssetId(), List.of(firstAssetType.getAssetTypeId()), List.of(firstAttributeType.getAttributeTypeId()), null, null, 0, 50).getResults().get(0).getAsset_id(), "first asset; first asset type; first attribute type; equals first asset"),
      () -> assertEquals(firstAsset.getAssetId(), assetsService.getAssetAttributeLinkUsage(secondAsset.getAssetId(), null, null, null, null, 0, 50).getResults().get(0).getAsset_id(), "second asset; equals first asset")
    );
  }

  @Test
  @Transactional
  public void getAssetHeader_Success_IntegrationTest () {
    UUID defaultRoleId = UUID.fromString("00000000-0000-0000-0000-000000005040");
    UUID defaultAttributeTypeId = UUID.fromString("00000000-0000-0000-0000-000000003114");
    createDefaultRole(defaultRoleId);
    createDefaultAttributeType(defaultAttributeTypeId);

    Optional<AttributeType> defaultAttributeType = attributeTypeRepository.findById(defaultAttributeTypeId);
    Optional<Role> defaultRole = roleRepository.findById(defaultRoleId);

    Status statusStewardship = statusRepository.save(new Status("stewardship status name", "desc", language, user));
    AssetType assetType = assetTypeRepository.save(new AssetType("asset type", "desc", "acr", "color", language, user));
    Asset asset = assetRepository.save(new Asset("asset", assetType, "asc", language, null, statusStewardship, user));
    Responsibility defaultResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, defaultRole.get(), ResponsibleType.USER, user));

    Attribute defaultAttribute = new Attribute(defaultAttributeType.get(), asset, language, user);
    defaultAttribute.setValue("7");
    attributeRepository.save(defaultAttribute);

    GetAssetHeaderResponse assetHeader = assetsService.getAssetHeader(asset.getAssetId());

    assertAll(
      () -> assertEquals(asset.getAssetName(), assetHeader.getAsset_name()),
      () -> assertEquals(defaultAttribute.getValue(), assetHeader.getDescription()),
      () -> assertEquals(1, assetHeader.getBusiness_owners().size()),
      () -> assertEquals(defaultResponsibility.getResponsibilityId(), assetHeader.getBusiness_owners().get(0).getResponsibility_id()),
      () -> assertEquals(user.getUserId(), assetHeader.getBusiness_owners().get(0).getResponsible_id()),
      () -> assertEquals(user.getUsername(), assetHeader.getBusiness_owners().get(0).getResponsible_name())
    );

    AttributeType anotherAttributeType = attributeTypeRepository.save(new AttributeType("new attribute type name", "desc", AttributeKindType.BOOLEAN, null, null, language, user));
    Attribute anotherAttribute = new Attribute(anotherAttributeType, asset, language, user);
    defaultAttribute.setValue("another");
    attributeRepository.save(anotherAttribute);
    customViewRepository.save(new CustomView(assetType, "some name", null, null, null, null, null, null, "select something from something", null, defaultRole.get(), user));

    Role anotherRole = roleRepository.save(new Role("another role name", "desc", language, user));
    User anotherUser = userRepository.save(new User("another username", "test@mail.com", "name", "second", SourceType.KEYCLOAK, null, UserType.SERVICE, UserWorkStatus.ACTIVE, null, language, user));
    Responsibility anotherResponsibility = responsibilityRepository.save(new Responsibility(anotherUser, null, asset, anotherRole, ResponsibleType.USER, user));

    assetTypeCardHeaderAssignmentRepository.save(new AssetTypeCardHeaderAssignment(assetType, anotherAttributeType, anotherRole, user));
    GetAssetHeaderResponse secondAssetHeader = assetsService.getAssetHeader(asset.getAssetId());

    assertAll(
      () -> assertEquals(asset.getAssetName(), secondAssetHeader.getAsset_name(), "asset name"),
      () -> assertEquals(anotherAttribute.getValue(), secondAssetHeader.getDescription(), "attribute value"),
      () -> assertTrue(secondAssetHeader.getCustom_views_flag(), "custom view"),
      () -> assertEquals(1, secondAssetHeader.getBusiness_owners().size(), "business owner size"),
      () -> assertEquals(anotherResponsibility.getResponsibilityId(), secondAssetHeader.getBusiness_owners().get(0).getResponsibility_id(), "another responsibility id"),
      () -> assertEquals(anotherUser.getUserId(), secondAssetHeader.getBusiness_owners().get(0).getResponsible_id(), "another user id"),
      () -> assertEquals(anotherUser.getUsername(), secondAssetHeader.getBusiness_owners().get(0).getResponsible_name(), "another username")
    );

    userRepository.delete(anotherUser);
  }

  @Test
  public void getAssetChangeHistory_Success_IntegrationTest () {
    Asset asset = assetRepository.save(new Asset("asset", assetTypeTwo, "asc", language, null, null, user));

    PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), null, null);

    // ASSET 3
    PostAssetResponse response = assetsService.createAsset(request, user);
    Optional<Asset> createdAsset = assetRepository.findById(response.getAsset_id());

    PostOrPatchAssetRequest patchRequest = new PostOrPatchAssetRequest(null, "new displayName", null, lifecycleStatus.getStatusId().toString(), null);
    assetsService.updateAsset(response.getAsset_id(), patchRequest, user);

    // STATUS - 2
    patchRequest = new PostOrPatchAssetRequest("new asset name", null,null, null, null);
    assetsService.updateAsset(response.getAsset_id(), patchRequest, user);

    patchRequest = new PostOrPatchAssetRequest(null, null, null, null, stewardshipStatus.getStatusId().toString());
    assetsService.updateAsset(response.getAsset_id(), patchRequest, user);

    // ATTRIBUTE 2
    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType("first attribute name", "desc", AttributeKindType.TEXT, null, null, language, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetTypeOne, firstAttributeType, user));
    PostAttributeResponse attribute = attributesService.createAttribute(new PostAttributeRequest(firstAttributeType.getAttributeTypeId().toString(), createdAsset.get().getAssetId(), "Some value"), user);
    attributesService.updateAttribute(attribute.getAttribute_id(), new PatchAttributeRequest("some new value"), user);

    // RELATION 1
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first relation type name", "description", 2, false, false, language, user));

    RelationTypeComponent firstFirstRelationType = relationTypeComponentRepository.save(new RelationTypeComponent("first first relation type component", "desc", null, null, null, language, firstRelationType, user));
    RelationTypeComponent firstSecondRelationType = relationTypeComponentRepository.save(new RelationTypeComponent("first second relation type component", "desc", null, null, null, language, firstRelationType, user));

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, firstFirstRelationType, createdAsset.get(), HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, firstSecondRelationType, asset, HierarchyRole.PARENT, null, user));

    // RELATION ATTRIBUTE 3
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(firstRelationType, firstAttributeType, user));
    PostRelationAttributeResponse relationAttribute = relationAttributesService.createRelationAttribute(new PostRelationAttributeRequest(firstAttributeType.getAttributeTypeId().toString(), relationWith2HierarchyComponents.getRelationId().toString(), "some value"), user);
    relationAttributesService.updateRelationAttribute(relationAttribute.getRelation_attribute_id(), new PatchRelationAttributeRequest("new relation attribute value"), user);
    relationAttributesService.deleteRelationAttributeById(relationAttribute.getRelation_attribute_id(), user);

    // RELATION COMPONENT ATTRIBUTE 2
    relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(firstFirstRelationType, firstAttributeType, user));
    PostRelationComponentAttributeResponse relationComponentAttribute = relationComponentAttributesService.createRelationComponentAttribute(new PostRelationComponentAttributeRequest(firstAttributeType.getAttributeTypeId().toString(), relationComponentChild.getRelationComponentId().toString(), "some value"), user);
    relationComponentAttributesService.updateRelationComponentAttribute(relationComponentAttribute.getRelation_component_attribute_id(), new PatchRelationComponentAttributeRequest("relation component attribute value"), user);

    Date maxDate = DateUtils.addDays(new Date(), 1);
    Date minDate = DateUtils.setYears(new Date(), 2024);

    assertAll(
      () -> assertEquals(2, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, null, List.of(AssetHistoryEntityType.STATUS), minDate, maxDate, 0, 25)).getTotal(), "Status total changes"),
      () -> assertEquals(3, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, null, List.of(AssetHistoryEntityType.ASSET), minDate, maxDate, 0, 25)).getTotal(), "Asset total changes"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.ADD), List.of(AssetHistoryEntityType.ASSET), minDate, maxDate, 0, 25)).getTotal(), "Asset add changes"),
      () -> assertEquals(response.getAsset_displayname(), assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.ADD), List.of(AssetHistoryEntityType.ASSET), minDate, maxDate, 0, 25)).getResults().get(0).getAction_details().getValue(), "Asset add changes asset_disaplayName"),
      () -> assertEquals(2, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.EDIT), List.of(AssetHistoryEntityType.ASSET), minDate, maxDate, 0, 25)).getTotal(), "Asset edit changes"),
      () -> assertEquals(2, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, null, List.of(AssetHistoryEntityType.ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Attribute total changes"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.ADD), List.of(AssetHistoryEntityType.ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Attribute add changes"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.EDIT), List.of(AssetHistoryEntityType.ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Attribute edit changes"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, null, List.of(AssetHistoryEntityType.RELATION), minDate, maxDate, 0, 25)).getTotal(), "Relation total changes"),
      () -> assertEquals(3, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, null, List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Relation attribute total changes"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.ADD), List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Relation attribute add changes"),
      () -> assertEquals(relationAttribute.getValue(), assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.ADD), List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE), minDate, maxDate, 0, 25)).getResults().get(0).getAction_details().getValue(), "Relation attribute add value"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.EDIT), List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Relation attribute edit changes"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.DELETE), List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Relation attribute delete changes"),
      () -> assertEquals(2, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, null, List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Relation component attribute total changes"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.ADD), List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Relation component attribute add changes"),
      () -> assertEquals(relationComponentAttribute.getValue(), assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.ADD), List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE), minDate, maxDate, 0, 25)).getResults().get(0).getAction_details().getValue(), "Relation component attribute add value"),
      () -> assertEquals(1, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, List.of(AssetHistoryActionType.EDIT), List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE), minDate, maxDate, 0, 25)).getTotal(), "Relation component attribute edit changes"),
      () -> assertEquals(0, assetsService.getAssetChangeHistory(response.getAsset_id(), new GetChangeHistoryParams(null, null, List.of(AssetHistoryEntityType.RESPONSIBILITY), minDate, maxDate, 0, 25)).getTotal(), "Relation component attribute total changes")
    );
  }

  @Test
  public void deleteAssetById_Success_IntegrationTest () {
    Asset assetWithNoStatus = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));

    assetsService.deleteAssetById(assetWithNoStatus.getAssetId(), user);

    Optional<Asset> asset = assetRepository.findById(assetWithNoStatus.getAssetId());

    assertAll(
      () -> assertNotNull(asset.get().getDeletedOn()),
      () -> assertTrue(asset.get().getIsDeleted())
    );
  }

  @Test
  public void deleteAssetById_AssetHistory_IntegrationTest () {
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("some name", "this will be shown", assetTypeOne.getAssetTypeId().toString(), null, null);
    PostAssetResponse postResponse = assetsService.createAsset(postRequest, user);

    assetsService.deleteAssetById(postResponse.getAsset_id(), user);

    Optional<Asset> deletedAsset = assetRepository.findById(postResponse.getAsset_id());

    List<AssetHistory> assetHistories = assetHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, assetHistories.size(), "asset history size"),
      () -> assertEquals(postResponse.getCreated_on(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.getAsset_displayname())).findFirst().get().getValidFrom(), "post asset history valid from"),
      () -> assertEquals(deletedAsset.get().getDeletedOn(), assetHistories.stream().filter(AssetHistory::getIsDeleted).findFirst().get().getValidFrom(), "deleted asset history valid from"),
      () -> assertEquals(deletedAsset.get().getDeletedOn(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.getAsset_displayname())).findFirst().get().getValidTo(), "post asset history valid to"),
      () -> assertEquals(deletedAsset.get().getDeletedOn(), assetHistories.stream().filter(AssetHistory::getIsDeleted).findFirst().get().getValidTo(), "deleted asset history valid to")
    );
  }

  @Test
  public void deleteAssetById_DeleteAssetLinkUsage_IntegrationTest () {
    Asset assetWithNoStatus = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));

    Attribute attribute = attributeRepository.save(new Attribute(null, assetWithNoStatus, language, null));
    AssetLinkUsage assetLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(attribute, assetWithNoStatus, user));

    assetsService.deleteAssetById(assetWithNoStatus.getAssetId(), user);

    Optional<AssetLinkUsage> deletedAssetLinkUsage = assetLinkUsageRepository.findById(assetLinkUsage.getAssetLinkUsageId());

    assertAll(
      () -> assertNotNull(deletedAssetLinkUsage.get().getDeletedOn()),
      () -> assertTrue(deletedAssetLinkUsage.get().getIsDeleted())
    );
  }

  @Test
  public void deleteAssetById_AssetNotFoundException_IntegrationTest () {
    assertThrows(AssetNotFoundException.class, () -> assetsService.deleteAssetById(new UUID(123, 123), user));
  }

  @Test
  public void deleteAssetById_DeleteConnectedResponsibilities_IntegrationTest () {
    Asset assetWithNoStatus = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Role role = roleRepository.save(new Role("role name", "some desc", null, user));
    Responsibility responsibility = responsibilityRepository.save(new Responsibility(null, null, assetWithNoStatus, role, ResponsibleType.USER, user));

    assetsService.deleteAssetById(assetWithNoStatus.getAssetId(), user);

    Optional<Responsibility> deletedResponsibility = responsibilityRepository.findById(responsibility.getResponsibilityId());
    assertAll(
      () -> assertTrue(deletedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetById_DeleteConnectedSubscriptions_IntegrationTest () {
    Asset assetWithNoStatus = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Subscription subscription = subscriptionRepository.save(new Subscription(user, assetWithNoStatus, "* */5 * * * *", user));

    assetsService.deleteAssetById(assetWithNoStatus.getAssetId(), user);

    Optional<Subscription> deletedSubscription = subscriptionRepository.findById(subscription.getSubscriptionId());
    assertAll(
      () -> assertTrue(deletedSubscription.get().getIsDeleted()),
      () -> assertNotNull(deletedSubscription.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedSubscription.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetById_DeleteConnectedAttributes_IntegrationTest () {
    Asset assetWithNoStatus = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType("first attribute name", "desc", AttributeKindType.TEXT, null, null, language, user));
    Attribute attribute = attributeRepository.save(new Attribute(firstAttributeType, assetWithNoStatus, language, user));

    assetsService.deleteAssetById(assetWithNoStatus.getAssetId(), user);

    Optional<Attribute> deletedAttribute = attributeRepository.findById(attribute.getAttributeId());
    assertAll(
      () -> assertTrue(deletedAttribute.get().getIsDeleted()),
      () -> assertNotNull(deletedAttribute.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedAttribute.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetById_DeleteConnectedRelations_IntegrationTest () {
    Asset firstAsset = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("asset one status", assetTypeOne, "stewardship name", language, null, null, user));

    RelationType relationTypeWith2Components = relationTypeRepository.save(new RelationType("name", "description", 2, false, true, language, user));
    RelationTypeComponent relationTypeComponentWithChildHierarchyRole = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, HierarchyRole.CHILD, null, language, relationTypeWith2Components, user));
    RelationTypeComponent relationTypeComponentWithParentHierarchyRole = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, HierarchyRole.PARENT, null, language, relationTypeWith2Components, user));

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    assetsService.deleteAssetById(firstAsset.getAssetId(), user);

    Optional<Relation> deletedRelationWith2HierarchyComponents = relationRepository.findById(relationWith2HierarchyComponents.getRelationId());
    Optional<RelationComponent> deletedRelationComponentChild = relationComponentRepository.findById(relationComponentChild.getRelationComponentId());
    Optional<RelationComponent> deletedRelationComponentParent = relationComponentRepository.findById(relationComponentParent.getRelationComponentId());

    assertAll(
      () -> assertTrue(deletedRelationWith2HierarchyComponents.get().getIsDeleted()),
      () -> assertNotNull(deletedRelationWith2HierarchyComponents.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelationWith2HierarchyComponents.get().getDeletedBy().getUserId()),
      () -> assertTrue(deletedRelationComponentChild.get().getIsDeleted()),
      () -> assertNotNull(deletedRelationComponentChild.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelationComponentChild.get().getDeletedBy().getUserId()),
      () -> assertTrue(deletedRelationComponentParent.get().getIsDeleted()),
      () -> assertNotNull(deletedRelationComponentParent.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelationComponentParent.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetsBulk_Success_IntegrationTest () {
    Asset firstAsset = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("asset one status", assetTypeOne, "stewardship name", language, null, null, user));

    assetsService.deleteAssetsBulk(List.of(firstAsset.getAssetId(), secondAsset.getAssetId()), user);

    Optional<Asset> firstDeletedAsset = assetRepository.findById(firstAsset.getAssetId());
    Optional<Asset> secondDeletedAsset = assetRepository.findById(secondAsset.getAssetId());

    assertAll(
      () -> assertNotNull(firstDeletedAsset.get().getDeletedOn()),
      () -> assertTrue(firstDeletedAsset.get().getIsDeleted()),
      () -> assertNotNull(secondDeletedAsset.get().getDeletedOn()),
      () -> assertTrue(secondDeletedAsset.get().getIsDeleted())
    );
  }

  @Test
  public void deleteAssetsBulk_AssetHistory_IntegrationTest () {
    PostOrPatchAssetRequest firstRequest = new PostOrPatchAssetRequest("first asset", "this will be shown", assetTypeOne.getAssetTypeId().toString(), lifecycleStatus.getStatusId().toString(), null);
    PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("some assets", "displayname", assetTypeTwo.getAssetTypeId().toString(), null, stewardshipStatus.getStatusId().toString());
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    requests.add(firstRequest);
    requests.add(secondRequest);

    List<PostAssetResponse> postResponse = assetsService.createAssetsBulk(requests, user);

    assetsService.deleteAssetsBulk(List.of(postResponse.get(0).getAsset_id(), postResponse.get(1).getAsset_id()), user);

    Optional<Asset> firstDeletedAsset = assetRepository.findById(postResponse.get(0).getAsset_id());
    Optional<Asset> secondDeletedAsset = assetRepository.findById(postResponse.get(1).getAsset_id());

    List<AssetHistory> assetHistories = assetHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(4, assetHistories.size(), "asset history size"),
      () -> assertEquals(postResponse.get(0).getCreated_on(), assetHistories.stream().filter(asset -> !asset.getIsDeleted() && asset.getAssetId().equals(postResponse.get(0).getAsset_id())).findFirst().get().getValidFrom(), "first post asset history valid from"),
      () -> assertEquals(postResponse.get(1).getCreated_on(), assetHistories.stream().filter(asset -> !asset.getIsDeleted() && asset.getAssetId().equals(postResponse.get(1).getAsset_id())).findFirst().get().getValidFrom(), "second post asset history valid from"),
      () -> assertEquals(firstDeletedAsset.get().getDeletedOn(), assetHistories.stream().filter(asset -> asset.getIsDeleted() && asset.getAssetId().equals(firstDeletedAsset.get().getAssetId())).findFirst().get().getValidFrom(), "first deleted asset history valid from"),
      () -> assertEquals(secondDeletedAsset.get().getDeletedOn(), assetHistories.stream().filter(asset -> asset.getIsDeleted() && asset.getAssetId().equals(secondDeletedAsset.get().getAssetId())).findFirst().get().getValidFrom(), "second deleted asset history valid from"),
      () -> assertEquals(firstDeletedAsset.get().getDeletedOn(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.get(0).getAsset_displayname())).findFirst().get().getValidTo(), "first post asset history valid to"),
      () -> assertEquals(secondDeletedAsset.get().getDeletedOn(), assetHistories.stream().filter(asset -> asset.getAssetDisplayName().equals(postResponse.get(1).getAsset_displayname())).findFirst().get().getValidTo(), "second post asset history valid to"),
      () -> assertEquals(firstDeletedAsset.get().getDeletedOn(), assetHistories.stream().filter(asset -> asset.getIsDeleted() && asset.getAssetId().equals(firstDeletedAsset.get().getAssetId())).findFirst().get().getValidTo(), "first deleted asset history valid to"),
      () -> assertEquals(secondDeletedAsset.get().getDeletedOn(), assetHistories.stream().filter(asset -> asset.getIsDeleted() && asset.getAssetId().equals(secondDeletedAsset.get().getAssetId())).findFirst().get().getValidTo(), "second deleted asset history valid to")
    );
  }

  @Test
  public void deleteAssetsBulk_AssetAlreadyDeleted_IntegrationTest () {
    Asset firstAsset = new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user);
    firstAsset.setIsDeleted(true);
    assetRepository.save(firstAsset);
    Asset secondAsset = assetRepository.save(new Asset("asset one status", assetTypeOne, "stewardship name", language, null, null, user));

    assertThrows(AssetNotFoundException.class, () -> assetsService.deleteAssetsBulk(List.of(firstAsset.getAssetId(), secondAsset.getAssetId()), user));
  }

  @Test
  public void deleteAssetBulk_DeleteConnectedSubscriptions_IntegrationTest () {
    Asset firstAsset = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("asset one status", assetTypeOne, "stewardship name", language, null, null, user));
    Subscription subscription = subscriptionRepository.save(new Subscription(user, firstAsset, "* */5 * * * *", user));

    assetsService.deleteAssetsBulk(List.of(firstAsset.getAssetId(), secondAsset.getAssetId()), user);

    Optional<Subscription> deletedSubscription = subscriptionRepository.findById(subscription.getSubscriptionId());
    assertAll(
      () -> assertTrue(deletedSubscription.get().getIsDeleted()),
      () -> assertNotNull(deletedSubscription.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedSubscription.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetBulk_DeleteConnectedResponsibilities_IntegrationTest () {
    Asset firstAsset = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("asset one status", assetTypeOne, "stewardship name", language, null, null, user));
    Role role = roleRepository.save(new Role("role name", "some desc", null, user));
    Responsibility responsibility = responsibilityRepository.save(new Responsibility(null, null, firstAsset, role, ResponsibleType.USER, user));

    assetsService.deleteAssetsBulk(List.of(firstAsset.getAssetId(), secondAsset.getAssetId()), user);

    Optional<Responsibility> deletedResponsibility = responsibilityRepository.findById(responsibility.getResponsibilityId());
    assertAll(
      () -> assertTrue(deletedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetBulk_DeleteConnectedAttributes_IntegrationTest () {
    Asset firstAsset = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("asset one status", assetTypeOne, "stewardship name", language, null, null, user));
    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType("first attribute name", "desc", AttributeKindType.TEXT, null, null, language, user));
    Attribute attribute = attributeRepository.save(new Attribute(firstAttributeType, firstAsset, language, user));

    assetsService.deleteAssetsBulk(List.of(firstAsset.getAssetId(), secondAsset.getAssetId()), user);

    Optional<Attribute> deletedAttribute = attributeRepository.findById(attribute.getAttributeId());
    assertAll(
      () -> assertTrue(deletedAttribute.get().getIsDeleted()),
      () -> assertNotNull(deletedAttribute.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedAttribute.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetBulk_DeleteConnectedRelations_IntegrationTest () {
    Asset firstAsset = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("asset one status", assetTypeOne, "stewardship name", language, null, null, user));

    RelationType relationTypeWith2Components = relationTypeRepository.save(new RelationType("name", "description", 2, false, true, language, user));
    RelationTypeComponent relationTypeComponentWithChildHierarchyRole = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, HierarchyRole.CHILD, null, language, relationTypeWith2Components, user));
    RelationTypeComponent relationTypeComponentWithParentHierarchyRole = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, HierarchyRole.PARENT, null, language, relationTypeWith2Components, user));

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    assetsService.deleteAssetsBulk(List.of(firstAsset.getAssetId(), secondAsset.getAssetId()), user);

    Optional<Relation> deletedRelationWith2HierarchyComponents = relationRepository.findById(relationWith2HierarchyComponents.getRelationId());
    Optional<RelationComponent> deletedRelationComponentChild = relationComponentRepository.findById(relationComponentChild.getRelationComponentId());
    Optional<RelationComponent> deletedRelationComponentParent = relationComponentRepository.findById(relationComponentParent.getRelationComponentId());

    assertAll(
      () -> assertTrue(deletedRelationWith2HierarchyComponents.get().getIsDeleted()),
      () -> assertNotNull(deletedRelationWith2HierarchyComponents.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelationWith2HierarchyComponents.get().getDeletedBy().getUserId()),
      () -> assertTrue(deletedRelationComponentChild.get().getIsDeleted()),
      () -> assertNotNull(deletedRelationComponentChild.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelationComponentChild.get().getDeletedBy().getUserId()),
      () -> assertTrue(deletedRelationComponentParent.get().getIsDeleted()),
      () -> assertNotNull(deletedRelationComponentParent.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelationComponentParent.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetBulk_DeleteConnectedAssetLink_IntegrationTest () {
    Asset firstAsset = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("asset one status", assetTypeOne, "stewardship name", language, null, null, user));

    Attribute firstAttribute = attributeRepository.save(new Attribute(null, firstAsset, language, null));
    AssetLinkUsage assetLinkUsage = assetLinkUsageRepository.save(new AssetLinkUsage(firstAttribute, firstAsset, user));
    assetsService.deleteAssetsBulk(List.of(firstAsset.getAssetId(), secondAsset.getAssetId()), user);

    Optional<AssetLinkUsage> deletedAssetLinkUsage = assetLinkUsageRepository.findById(assetLinkUsage.getAssetLinkUsageId());
    assertAll(
      () -> assertTrue(deletedAssetLinkUsage.get().getIsDeleted()),
      () -> assertNotNull(deletedAssetLinkUsage.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedAssetLinkUsage.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAssetsBulk_DuplicateValueInRequestException_IntegrationTest () {
    Asset firstAsset = assetRepository.save(new Asset("empty status", assetTypeTwo, "stewardship name", language, null, null, user));

    assertThrows(DuplicateValueInRequestException.class, () -> assetsService.deleteAssetsBulk(List.of(firstAsset.getAssetId(), firstAsset.getAssetId()), user));
  }

  private void generateAssets (int count) {
    for (int i = 0; i < count; i++) {
      assetRepository.save(new Asset("asset №" + i, assetTypeTwo, i + "", language, null, null, user));
    }
  }

  private void createDefaultRole (UUID roleId) {
    String createRole = "Insert into role (role_id, source_language, role_name, role_description, created_by) VALUES(:roleId, :sourceLanguage, :roleName, :roleDescription, :createdBy)";
    Query createRoleQuery = entityManager.createNativeQuery(createRole);
    createRoleQuery.setParameter("roleId", roleId);
    createRoleQuery.setParameter("sourceLanguage", language.getLanguageId());
    createRoleQuery.setParameter("roleName", "defaultRole name");
    createRoleQuery.setParameter("roleDescription", "desc");
    createRoleQuery.setParameter("createdBy", user.getUserId());

    createRoleQuery.executeUpdate();
  }

  private void createDefaultAttributeType (UUID attributeTypeId) {
    String createAttributeType = "Insert into attribute_type (attribute_type_id, attribute_type_name, attribute_type_description, attribute_kind, source_language, created_by) VALUES(:attributeTypeId, :attributeTypeName, :description, :attributeKind, :sourceLanguage, :createdBy)";
    Query createAttributeTypeQuery = entityManager.createNativeQuery(createAttributeType);
    createAttributeTypeQuery.setParameter("attributeTypeId", attributeTypeId);
    createAttributeTypeQuery.setParameter("attributeTypeName", "default attribute type");
    createAttributeTypeQuery.setParameter("description", "desc");
    createAttributeTypeQuery.setParameter("attributeKind", AttributeKindType.TEXT.toString());
    createAttributeTypeQuery.setParameter("sourceLanguage", language.getLanguageId());
    createAttributeTypeQuery.setParameter("createdBy", user.getUserId());

    createAttributeTypeQuery.executeUpdate();
  }
}
