package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class ResponsibilitiesPermissionTest extends MockPermissionTest {
  @Autowired
  private ResponsibilityRepository responsibilityRepository;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RoleActionRepository roleActionRepository;

  @Autowired
  private ActionTypeRepository actionTypeRepository;

  @Autowired
  private EntityRepository entityRepository;

  @Autowired
  private LogRepository logRepository;

  @Autowired
  private RoleActionCachingService roleActionCachingService;

  Role role;
  Role assetRole;
  Asset asset;
  Asset secondAsset;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;
  ActionType grantActionType;
  ActionType revokeActionType;

  Entity responsibilityEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));
    assetRole = roleRepository.save(new Role("test asset name", "desc", language, user));

    AssetType assetType = assetTypeRepository.save(new AssetType("some name", "desc", "acr", "color", language, user));
    asset = assetRepository.save(new Asset("asset name", assetType, "disp", language, null, null, user));

    AssetType secondAssetType = assetTypeRepository.save(new AssetType("second asset type name", "desc", "acr", "color", language, user));
    secondAsset = assetRepository.save(new Asset("second asset name", secondAssetType, "disp", language, null, null, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));
    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, assetRole, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();
    grantActionType = actionTypeRepository.findById(UUID.fromString("d3bfacc2-9438-4296-a149-cfc6287e627d")).get();
    revokeActionType = actionTypeRepository.findById(UUID.fromString("5b197bb3-5140-4acf-b576-7870205b4342")).get();

    responsibilityEntity = entityRepository.findById(UUID.fromString("f55aa87a-d324-4d1e-891d-26ee931bcd14")).get();
  }

  @AfterAll
  public void clearData () {
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    responsibilityRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_responsibilities_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostResponsibilityRequest postRequest = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/responsibilities",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_responsibilities_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostResponsibilityRequest postRequest = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/responsibilities",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_responsibilities_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_roleType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostResponsibilityRequest postRequest = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    RoleAction roleAction = new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/responsibilities",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_responsibilities_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_roleType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostResponsibilityRequest postRequest = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    RoleAction roleAction = new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/responsibilities",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_responsibilities_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostResponsibilityRequest> requests = new ArrayList<>();
    requests.add(new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId()));
    requests.add(new PostResponsibilityRequest(secondAsset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId()));
    String requestBody = new ObjectMapper().writeValueAsString(requests);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/responsibilities/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_responsibilities_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostResponsibilityRequest> requests = new ArrayList<>();
    requests.add(new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId()));
    requests.add(new PostResponsibilityRequest(secondAsset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId()));
    String requestBody = new ObjectMapper().writeValueAsString(requests);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/responsibilities/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_responsibilities_bulk_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_roleType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostResponsibilityRequest> requests = new ArrayList<>();
    requests.add(new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId()));
    requests.add(new PostResponsibilityRequest(secondAsset.getAssetId(), assetRole.getRoleId(), ResponsibleType.USER.toString(), user.getUserId()));
    String requestBody = new ObjectMapper().writeValueAsString(requests);

    roleActionRepository.save(new RoleAction(assetRole, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    RoleAction roleAction = new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/responsibilities/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_responsibilities_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_roleType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostResponsibilityRequest> requests = new ArrayList<>();
    requests.add(new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), ResponsibleType.USER.toString(), user.getUserId()));
    requests.add(new PostResponsibilityRequest(secondAsset.getAssetId(), assetRole.getRoleId(), ResponsibleType.USER.toString(), user.getUserId()));
    String requestBody = new ObjectMapper().writeValueAsString(requests);

    roleActionRepository.save(new RoleAction(assetRole, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, grantActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/responsibilities/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_responsibilities_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/responsibilities",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_responsibilities_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/responsibilities",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_responsibilities_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/responsibilities/" + responsibility.getResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_responsibilities_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/responsibilities/" + responsibility.getResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_responsibilities_by_id_GlobalResponsibility_DENY_ONE_ID_roleType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/responsibilities/" + responsibility.getResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_responsibilities_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(role, revokeActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/responsibilities/" + responsibility.getResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_responsibilities_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(role, revokeActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/responsibilities/" + responsibility.getResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_responsibilities_GlobalResponsibility_DENY_ONE_ID_roleType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, revokeActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, revokeActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/responsibilities/" + responsibility.getResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_responsibilities_GlobalResponsibility_ALLOW_ONE_ID_roleType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, revokeActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, revokeActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/responsibilities/" + responsibility.getResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_responsibilities_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Responsibility firstResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));
    Responsibility secondResponsibility = responsibilityRepository.save(new Responsibility(user, null, secondAsset, role, ResponsibleType.USER, user));

    List<UUID> responsibilitiesIds = List.of(firstResponsibility.getResponsibilityId(), secondResponsibility.getResponsibilityId());
    String requestBody = new ObjectMapper().writeValueAsString(responsibilitiesIds);

    roleActionRepository.save(new RoleAction(role, revokeActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/responsibilities/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_responsibilities_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Responsibility firstResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));
    Responsibility secondResponsibility = responsibilityRepository.save(new Responsibility(user, null, secondAsset, role, ResponsibleType.USER, user));

    List<UUID> responsibilitiesIds = List.of(firstResponsibility.getResponsibilityId(), secondResponsibility.getResponsibilityId());
    String requestBody = new ObjectMapper().writeValueAsString(responsibilitiesIds);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/responsibilities/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_responsibilities_bulk_GlobalResponsibility_DENY_ONE_ID_roleType_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Responsibility firstResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));
    Responsibility secondResponsibility = responsibilityRepository.save(new Responsibility(user, null, secondAsset, assetRole, ResponsibleType.USER, user));

    List<UUID> responsibilitiesIds = List.of(firstResponsibility.getResponsibilityId(), secondResponsibility.getResponsibilityId());
    String requestBody = new ObjectMapper().writeValueAsString(responsibilitiesIds);

    roleActionRepository.save(new RoleAction(assetRole, revokeActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, revokeActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/responsibilities/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_responsibilities_bulk_GlobalResponsibility_ALLOW_ONE_ID_roleType_responsibility_DENY_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Responsibility firstResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));
    Responsibility secondResponsibility = responsibilityRepository.save(new Responsibility(user, null, secondAsset, assetRole, ResponsibleType.USER, user));

    List<UUID> responsibilitiesIds = List.of(firstResponsibility.getResponsibilityId(), secondResponsibility.getResponsibilityId());
    String requestBody = new ObjectMapper().writeValueAsString(responsibilitiesIds);

    roleActionRepository.save(new RoleAction(assetRole, revokeActionType, responsibilityEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, revokeActionType, responsibilityEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/responsibilities/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }
}
