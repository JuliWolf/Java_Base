package ru.leroymerlin.datamanagement.spring.data_catalog_backend.aspects;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups.GroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusRequest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author juliwolf
 */

public class LoggerAspectConfigurationTest extends MockPermissionTest {
  @Autowired
  private LogRepository logRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RoleActionRepository roleActionRepository;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @Autowired
  private ActionTypeRepository actionTypeRepository;
  @Autowired
  private EntityRepository entityRepository;

  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private GroupRepository groupRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private StatusRepository statusRepository;

  @BeforeAll
  public void addRole () {
    Optional<ActionType> addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Optional<ActionType> editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee"));
    Optional<ActionType> deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a"));

    Optional<Entity> assetEntity = entityRepository.findById(UUID.fromString("f2d482f5-fe31-45c8-856d-512a9aa56fde"));
    Optional<Entity> groupEntity = entityRepository.findById(UUID.fromString("7111c0aa-ed81-45d9-8f59-155e9e5f2788"));
    Optional<Entity> statusEntity = entityRepository.findById(UUID.fromString("551048d8-4a63-47fa-af63-9c8b46e22da5"));


    Role role = roleRepository.save(new Role("ADMIN", "ADMIN", language, user));
    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(role, addActionType.get(), assetEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, editActionType.get(), assetEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, deleteActionType.get(), assetEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));

    roleActionRepository.save(new RoleAction(role, addActionType.get(), groupEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, editActionType.get(), groupEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, deleteActionType.get(), groupEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));

    roleActionRepository.save(new RoleAction(role, addActionType.get(), statusEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, editActionType.get(), statusEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, deleteActionType.get(), statusEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
  }

  @AfterAll
  public void clearRoles () {
    roleActionRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @AfterEach
  public void clearLog () {
    statusRepository.deleteAll();
    logRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    groupRepository.deleteAll();
  }

  @Test
  public void postAssetRequestTest () throws JsonProcessingException {
    AssetType assetType = assetTypeRepository.save(new AssetType("test asset type", "desc", "acr", "color", language, user));
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets",
      requestBody,
      user
    );

    assertAll(
      () -> assertEquals(1, logRepository.findAll().size()),
      () -> assertEquals(request.getStatusCode(), logRepository.findAll().get(0).getResponseCode())
    );
  }

  @Test
  public void patchAssetRequestTest () throws JsonProcessingException {
    AssetType assetType = assetTypeRepository.save(new AssetType("test asset type", "desc", "acr", "color", language, user));
    Asset asset = assetRepository.save(new Asset("asset name", assetType, "an", language, null, null, user));

    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      requestBody,
      user
    );

    assertAll(
      () -> assertEquals(1, logRepository.findAll().size()),
      () -> assertEquals(request.getStatusCode(), logRepository.findAll().get(0).getResponseCode())
    );
  }

  @Test
  public void deleteAssetRequestTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("test asset type", "desc", "acr", "color", language, user));
    Asset asset = assetRepository.save(new Asset("asset name", assetType, "an", language, null, null, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertAll(
      () -> assertEquals(1, logRepository.findAll().size()),
      () -> assertEquals(request.getStatusCode(), logRepository.findAll().get(0).getResponseCode())
    );
  }

  @Test
  public void getAssetRequestTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("test asset type", "desc", "acr", "color", language, user));
    Asset asset = assetRepository.save(new Asset("asset name", assetType, "an", language, null, null, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets",
      null,
      user
    );

    assertEquals(0, logRepository.findAll().size());
  }

  @Test
  public void postGroupRequestTest () throws JsonProcessingException {
    PostGroupRequest postRequest = new PostGroupRequest("groupName", "desc", "email", "LOOP");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/groups",
      requestBody,
      user
    );

    assertAll(
      () -> assertEquals(1, logRepository.findAll().size()),
      () -> assertEquals(request.getStatusCode(), logRepository.findAll().get(0).getResponseCode())
    );
  }

  @Test
  public void deleteGroupRequestTest () {
    Group group = groupRepository.save(new Group("test group", "description", "test@email.com", "SLACK", user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/groups/" + group.getGroupId(),
      null,
      user
    );

    assertAll(
      () -> assertEquals(1, logRepository.findAll().size()),
      () -> assertEquals(request.getStatusCode(), logRepository.findAll().get(0).getResponseCode())
    );
  }

  @Test
  public void getGroupRequestTest () {
    Group group = groupRepository.save(new Group("test group", "description", "test@email.com", "SLACK", user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/groups",
      null,
      user
    );

    assertEquals(0, logRepository.findAll().size());
  }

  @Test
  public void postStatusRequestTest () throws JsonProcessingException {
    PostStatusRequest postRequest = new PostStatusRequest("new status", "new desc");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/statuses",
      requestBody,
      user
    );

    assertAll(
      () -> assertEquals(1, logRepository.findAll().size()),
      () -> assertEquals(request.getStatusCode(), logRepository.findAll().get(0).getResponseCode())
    );
  }

  @Test
  public void patchStatusRequestTest () {
    Status status = statusRepository.save(new Status("new status", "new desc", language, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/statuses/" + status.getStatusId(),
      "{\"status_name\":\"status\"}",
      user
    );

    assertAll(
      () -> assertEquals(1, logRepository.findAll().size()),
      () -> assertEquals(request.getStatusCode(), logRepository.findAll().get(0).getResponseCode())
    );
  }

  @Test
  public void deleteStatusRequestTest () {
    Status status = statusRepository.save(new Status("new status", "new desc", language, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/statuses/" + status.getStatusId(),
      null,
      user
    );

    assertAll(
      () -> assertEquals(1, logRepository.findAll().size()),
      () -> assertEquals(request.getStatusCode(), logRepository.findAll().get(0).getResponseCode())
    );
  }

  @Test
  public void getStatusRequestTest () {
    Status status = statusRepository.save(new Status("new status", "new desc", language, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/statuses",
      null,
      user
    );

    assertEquals(0, logRepository.findAll().size());
  }
}
