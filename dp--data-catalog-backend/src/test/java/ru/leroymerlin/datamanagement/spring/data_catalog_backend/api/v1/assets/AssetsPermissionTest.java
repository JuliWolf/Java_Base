package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class AssetsPermissionTest extends MockPermissionTest {
  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @Autowired
  private ResponsibilityRepository responsibilityRepository;

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
  AssetType assetType;
  AssetType secondAssetType;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity assetEntity;
  Entity assetTypeEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));
    assetRole = roleRepository.save(new Role("test asset name", "desc", language, user));

    assetType = assetTypeRepository.save(new AssetType("asset type name", "desc", "atn", "red", language, user));
    secondAssetType = assetTypeRepository.save(new AssetType("second asset type name", "desc", "atn", "red", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));
    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, assetRole, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    assetEntity = entityRepository.findById(UUID.fromString("f2d482f5-fe31-45c8-856d-512a9aa56fde")).get();
    assetTypeEntity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf")).get();
  }

  @AfterAll
  public void clearData () {
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    assetTypeRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    responsibilityRepository.deleteAll();
    roleActionRepository.deleteAll();
    assetRepository.deleteAll();
  }

  @Test
  public void POST_assets_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_assets_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_assets_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_assets_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_assets_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostOrPatchAssetRequest> assetsRequests = new ArrayList<>();
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    assetsRequests.add(postRequest);
    String requestBody = new ObjectMapper().writeValueAsString(assetsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_assets_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostOrPatchAssetRequest> assetsRequests = new ArrayList<>();
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    assetsRequests.add(postRequest);
    String requestBody = new ObjectMapper().writeValueAsString(assetsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_assets_bulk_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostOrPatchAssetRequest> assetsRequests = new ArrayList<>();
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    assetsRequests.add(postRequest);
    String requestBody = new ObjectMapper().writeValueAsString(assetsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_assets_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostOrPatchAssetRequest> assetsRequests = new ArrayList<>();
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    assetsRequests.add(postRequest);
    String requestBody = new ObjectMapper().writeValueAsString(assetsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_assets_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_DENY_Second_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostOrPatchAssetRequest> assetsRequests = new ArrayList<>();
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    PostOrPatchAssetRequest secondPostRequest = new PostOrPatchAssetRequest("second test asset name", "test displayName", secondAssetType.getAssetTypeId().toString(), null, null);
    assetsRequests.add(postRequest);
    assetsRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(assetsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);
    RoleAction secondRoleAction = new RoleAction(role, addActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(secondAssetType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_assets_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType_ALLOW_Second_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostOrPatchAssetRequest> assetsRequests = new ArrayList<>();
    PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest("test asset name", "test displayName", assetType.getAssetTypeId().toString(), null, null);
    PostOrPatchAssetRequest secondPostRequest = new PostOrPatchAssetRequest("second test asset name", "test displayName", secondAssetType.getAssetTypeId().toString(), null, null);
    assetsRequests.add(postRequest);
    assetsRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(assetsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);
    RoleAction secondRoleAction = new RoleAction(role, addActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setAssetType(secondAssetType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    PostOrPatchAssetRequest patchRequest = new PostOrPatchAssetRequest("test asset name", null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    PostOrPatchAssetRequest patchRequest = new PostOrPatchAssetRequest("test asset name", null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    PostOrPatchAssetRequest patchRequest = new PostOrPatchAssetRequest("test asset name", null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    PostOrPatchAssetRequest patchRequest = new PostOrPatchAssetRequest("test asset name", null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    PostOrPatchAssetRequest patchRequest = new PostOrPatchAssetRequest("test asset name", null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    PostOrPatchAssetRequest patchRequest = new PostOrPatchAssetRequest("test asset name", null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name", assetType, "disp", language, null, null, user));

    String requestBody = new ObjectMapper().writeValueAsString(List.of(
      new PatchAssetRequest(asset.getAssetId(), "test asset name", null, null, null, null),
      new PatchAssetRequest(secondAsset.getAssetId(), "second test asset name", null, null, null, null)
    ));

    roleActionRepository.save(new RoleAction(role, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name", secondAssetType, "disp", language, null, null, user));

    String requestBody = new ObjectMapper().writeValueAsString(List.of(
      new PatchAssetRequest(asset.getAssetId(), "test asset name", null, null, null, null),
      new PatchAssetRequest(secondAsset.getAssetId(), "second test asset name", null, null, null, null)
    ));

    roleActionRepository.save(new RoleAction(role, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_bulk_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name", secondAssetType, "disp", language, null, null, user));

    String requestBody = new ObjectMapper().writeValueAsString(List.of(
      new PatchAssetRequest(asset.getAssetId(), "test asset name", null, null, null, null),
      new PatchAssetRequest(secondAsset.getAssetId(), "second test asset name", null, null, null, null)
    ));

    roleActionRepository.save(new RoleAction(role, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType_ALLOW_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name", secondAssetType, "disp", language, null, null, user));

    String requestBody = new ObjectMapper().writeValueAsString(List.of(
      new PatchAssetRequest(asset.getAssetId(), "test asset name", null, null, null, null),
      new PatchAssetRequest(secondAsset.getAssetId(), "second test asset name", null, null, null, null)
    ));

    responsibilityRepository.save(new Responsibility(user, null, secondAsset, assetRole, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(assetRole, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    roleActionRepository.save(new RoleAction(role, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_bulk_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name", secondAssetType, "disp", language, null, null, user));

    String requestBody = new ObjectMapper().writeValueAsString(List.of(
      new PatchAssetRequest(asset.getAssetId(), "test asset name", null, null, null, null),
      new PatchAssetRequest(secondAsset.getAssetId(), "second test asset name", null, null, null, null)
    ));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_assets_bulk_GlobalResponsibility_DENY_ONE_ID_assetType_DENY_ONE_ID_assetType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second name", secondAssetType, "disp", language, null, null, user));

    String requestBody = new ObjectMapper().writeValueAsString(List.of(
      new PatchAssetRequest(asset.getAssetId(), "test asset name", null, secondAssetType.getAssetTypeId().toString(), null, null),
      new PatchAssetRequest(secondAsset.getAssetId(), "second test asset name", null, assetType.getAssetTypeId().toString(), null, null)
    ));

    RoleAction roleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);
    RoleAction secondRoleAction = new RoleAction(role, editActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    secondRoleAction.setAssetType(secondAssetType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_by_id_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_by_id_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_by_id_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_by_id_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_children_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/children",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_children_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/children",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_children_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/children",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_children_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/children",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_children_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/children",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_children_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/children",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_path_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/path",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_path_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/path",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_path_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/path",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_path_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/path",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_path_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/path",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_path_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/path",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_links_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/links",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_links_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/links",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_links_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/links",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_links_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/links",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_links_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/links",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_links_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/links",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();
    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();
    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_relationTypes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/relationTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_relationTypes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/relationTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_relationTypes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/relationTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_relationTypes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/relationTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_relationTypes_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/relationTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_relationTypes_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/relationTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_header_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/header",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_header_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/header",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_header_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/header",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_header_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/header",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_header_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/header",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_header_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/header",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_relationTypes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_changeHistory_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_changeHistory_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_changeHistory_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assets_changeHistory_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_changeHistory_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assets_changeHistory_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second some name", assetType, "disp", language, null, null, user));

    List<UUID> assetIds = List.of(asset.getAssetId(), secondAsset.getAssetId());
    String requestBody = new ObjectMapper().writeValueAsString(assetIds);

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_bulk_relationTypes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second some name", assetType, "disp", language, null, null, user));

    List<UUID> assetIds = List.of(asset.getAssetId(), secondAsset.getAssetId());
    String requestBody = new ObjectMapper().writeValueAsString(assetIds);

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_bulk_relationTypes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second some name", secondAssetType, "disp", language, null, null, user));

    List<UUID> assetIds = List.of(asset.getAssetId(), secondAsset.getAssetId());
    String requestBody = new ObjectMapper().writeValueAsString(assetIds);

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction secondRoleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    secondRoleAction.setAssetType(secondAssetType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_bulk_relationTypes_GlobalResponsibility_DENY_ONE_ID_relationType_DENY_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second some name", secondAssetType, "disp", language, null, null, user));

    List<UUID> assetIds = List.of(asset.getAssetId(), secondAsset.getAssetId());
    String requestBody = new ObjectMapper().writeValueAsString(assetIds);

    RoleAction roleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);
    RoleAction secondRoleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    secondRoleAction.setAssetType(secondAssetType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_bulk_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId_responsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second some name", assetType, "disp", language, null, null, user));

    List<UUID> assetIds = List.of(asset.getAssetId(), secondAsset.getAssetId());
    String requestBody = new ObjectMapper().writeValueAsString(assetIds);

    responsibilityRepository.save(new Responsibility(user, null, secondAsset, assetRole, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_assets_bulk_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId_globalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Asset asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second some name", secondAssetType, "disp", language, null, null, user));

    List<UUID> assetIds = List.of(asset.getAssetId(), secondAsset.getAssetId());
    String requestBody = new ObjectMapper().writeValueAsString(assetIds);

    responsibilityRepository.save(new Responsibility(user, null, secondAsset, role, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(secondAssetType);
    roleActionRepository.save(roleAction);

    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
