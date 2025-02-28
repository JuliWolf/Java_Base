package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchBulkAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class AttributesPermissionTest extends MockPermissionTest {
  @Autowired
  private AttributeRepository attributeRepository;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;

  @Autowired
  private ResponsibilityRepository responsibilityRepository;

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
  AttributeType attributeType;
  AttributeType secondAttributeType;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity attributeEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));
    assetRole = roleRepository.save(new Role("test asset name", "desc", language, user));

    AssetType assetType = assetTypeRepository.save(new AssetType("test asset type", "desc", "acr", "color", language, user));
    asset = assetRepository.save(new Asset("some new asset", assetType, "disp", language, null, null, user));
    secondAsset = assetRepository.save(new Asset("second asset", assetType, "disp", language, null, null, user));
    attributeType = attributeTypeRepository.save(new AttributeType("some attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));
    secondAttributeType = attributeTypeRepository.save(new AttributeType("second attribute type name", "desc", AttributeKindType.BOOLEAN, null, null, language, user));

    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, secondAttributeType, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));
    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, assetRole, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    attributeEntity = entityRepository.findById(UUID.fromString("18abdf45-f906-4fc3-bb2a-f8c05bf536b6")).get();
  }

  @AfterAll
  public void clearData () {
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    responsibilityRepository.deleteAll();
    attributeRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_attributes_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_attributes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_attributes_GlobalResponsibility_DENY_ONE_ID_responsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_attributes_GlobalResponsibility_DENY_ONE_ID_responsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_attributes_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAttributeRequest> attributesRequests = new ArrayList<>();
    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    PostAttributeRequest secondPostRequest = new PostAttributeRequest(secondAttributeType.getAttributeTypeId().toString(), secondAsset.getAssetId(), "true");
    attributesRequests.add(postRequest);
    attributesRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_attributes_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAttributeRequest> attributesRequests = new ArrayList<>();
    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    PostAttributeRequest secondPostRequest = new PostAttributeRequest(secondAttributeType.getAttributeTypeId().toString(), secondAsset.getAssetId(), "true");
    attributesRequests.add(postRequest);
    attributesRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_attributes_bulk_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAttributeRequest> attributesRequests = new ArrayList<>();
    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    PostAttributeRequest secondPostRequest = new PostAttributeRequest(secondAttributeType.getAttributeTypeId().toString(), secondAsset.getAssetId(), "true");
    attributesRequests.add(postRequest);
    attributesRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);
    RoleAction secondRoleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setAttributeType(secondAttributeType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_attributes_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAttributeRequest> attributesRequests = new ArrayList<>();
    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    PostAttributeRequest secondPostRequest = new PostAttributeRequest(secondAttributeType.getAttributeTypeId().toString(), secondAsset.getAssetId(), "true");
    attributesRequests.add(postRequest);
    attributesRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);
    RoleAction secondRoleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setAttributeType(secondAttributeType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_attributes_bulk_GlobalResponsibility_DENY_ONE_ID_responsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAttributeRequest> attributesRequests = new ArrayList<>();
    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    PostAttributeRequest secondPostRequest = new PostAttributeRequest(secondAttributeType.getAttributeTypeId().toString(), secondAsset.getAssetId(), "true");
    attributesRequests.add(postRequest);
    attributesRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_attributes_bulk_GlobalResponsibility_DENY_ONE_ID_responsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAttributeRequest> attributesRequests = new ArrayList<>();
    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    PostAttributeRequest secondPostRequest = new PostAttributeRequest(secondAttributeType.getAttributeTypeId().toString(), secondAsset.getAssetId(), "true");
    attributesRequests.add(postRequest);
    attributesRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_attributes_bulk_responsibility_DENY_ALL_responsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAttributeRequest> attributesRequests = new ArrayList<>();
    PostAttributeRequest postRequest = new PostAttributeRequest(attributeType.getAttributeTypeId().toString(), asset.getAssetId(), "some value");
    PostAttributeRequest secondPostRequest = new PostAttributeRequest(secondAttributeType.getAttributeTypeId().toString(), secondAsset.getAssetId(), "true");
    attributesRequests.add(postRequest);
    attributesRequests.add(secondPostRequest);
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, role, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    roleActionRepository.save(new RoleAction(role, addActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    PatchAttributeRequest patchRequest = new PatchAttributeRequest("some new value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    PatchAttributeRequest patchRequest = new PatchAttributeRequest("some new value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    PatchAttributeRequest patchRequest = new PatchAttributeRequest("some new value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_GlobalResponsibility_DENY_ONE_ID_attributeType_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    PatchAttributeRequest patchRequest = new PatchAttributeRequest("some new value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_GlobalResponsibility_ALLOW_ONE_ID_attributeType_responsibility_DENY_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    PatchAttributeRequest patchRequest = new PatchAttributeRequest("some new value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_GlobalResponsibility_ALLOW_ALL_responsibility_DENY_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    PatchAttributeRequest patchRequest = new PatchAttributeRequest("some new value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<PatchBulkAttributeRequest> attributesRequests = List.of(
      new PatchBulkAttributeRequest(attribute.getAttributeId(), "new text"),
      new PatchBulkAttributeRequest(secondAttribute.getAttributeId(), "false")
    );
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<PatchBulkAttributeRequest> attributesRequests = List.of(
      new PatchBulkAttributeRequest(attribute.getAttributeId(), "new text"),
      new PatchBulkAttributeRequest(secondAttribute.getAttributeId(), "false")
    );
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_bulk_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<PatchBulkAttributeRequest> attributesRequests = List.of(
      new PatchBulkAttributeRequest(attribute.getAttributeId(), "new text"),
      new PatchBulkAttributeRequest(secondAttribute.getAttributeId(), "false")
    );
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<PatchBulkAttributeRequest> attributesRequests = List.of(
      new PatchBulkAttributeRequest(attribute.getAttributeId(), "new text"),
      new PatchBulkAttributeRequest(secondAttribute.getAttributeId(), "false")
    );
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<PatchBulkAttributeRequest> attributesRequests = List.of(
      new PatchBulkAttributeRequest(attribute.getAttributeId(), "new text"),
      new PatchBulkAttributeRequest(secondAttribute.getAttributeId(), "false")
    );
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    RoleAction secondRoleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setAttributeType(secondAttributeType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_bulk_GlobalResponsibility_ALLOW_ONE_ID_attributeType_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<PatchBulkAttributeRequest> attributesRequests = List.of(
      new PatchBulkAttributeRequest(attribute.getAttributeId(), "new text"),
      new PatchBulkAttributeRequest(secondAttribute.getAttributeId(), "false")
    );
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_bulk_GlobalResponsibility_DENY_ONE_ID_attributeType_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<PatchBulkAttributeRequest> attributesRequests = List.of(
      new PatchBulkAttributeRequest(attribute.getAttributeId(), "new text"),
      new PatchBulkAttributeRequest(secondAttribute.getAttributeId(), "false")
    );
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributes_bulk_GlobalResponsibility_DENY_ALL_responsibility_ALLOW_ONE_ID_assetId_responsibility_ALLOW_ONE_ID_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<PatchBulkAttributeRequest> attributesRequests = List.of(
      new PatchBulkAttributeRequest(attribute.getAttributeId(), "new text"),
      new PatchBulkAttributeRequest(secondAttribute.getAttributeId(), "false")
    );
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, role, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, editActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    RoleAction secondRoleAction = new RoleAction(role, editActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setAttributeType(secondAttributeType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_attributes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_attributes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_attributes_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_attributes_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_attributes_by_id_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_attributes_by_id_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_attributes_by_id_GlobalResponsibility_DENY_ONE_ID_attributeType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_attributes_by_id_GlobalResponsibility_ALLOW_ONE_ID_attributeType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_GlobalResponsibility_ALLOW_ONE_ID_attributeType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_GlobalResponsibility_DENY_ONE_ID_attributeType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/" + attribute.getAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<UUID> attributesRequests = new ArrayList<>();
    attributesRequests.add(attribute.getAttributeId());
    attributesRequests.add(secondAttribute.getAttributeId());
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<UUID> attributesRequests = new ArrayList<>();
    attributesRequests.add(attribute.getAttributeId());
    attributesRequests.add(secondAttribute.getAttributeId());
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_bulk_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<UUID> attributesRequests = new ArrayList<>();
    attributesRequests.add(attribute.getAttributeId());
    attributesRequests.add(secondAttribute.getAttributeId());
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<UUID> attributesRequests = new ArrayList<>();
    attributesRequests.add(attribute.getAttributeId());
    attributesRequests.add(secondAttribute.getAttributeId());
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<UUID> attributesRequests = new ArrayList<>();
    attributesRequests.add(attribute.getAttributeId());
    attributesRequests.add(secondAttribute.getAttributeId());
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    RoleAction secondRoleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setAttributeType(secondAttributeType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_bulk_GlobalResponsibility_ALLOW_ONE_ID_attributeType_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<UUID> attributesRequests = new ArrayList<>();
    attributesRequests.add(attribute.getAttributeId());
    attributesRequests.add(secondAttribute.getAttributeId());
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_bulk_GlobalResponsibility_DENY_ONE_ID_attributeType_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<UUID> attributesRequests = new ArrayList<>();
    attributesRequests.add(attribute.getAttributeId());
    attributesRequests.add(secondAttribute.getAttributeId());
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributes_bulk_GlobalResponsibility_DENY_ALL_responsibility_ALLOW_ONE_ID_assetId_responsibility_ALLOW_ONE_ID_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));
    Attribute secondAttribute = attributeRepository.save(new Attribute(secondAttributeType, secondAsset, language, user));

    List<UUID> attributesRequests = new ArrayList<>();
    attributesRequests.add(attribute.getAttributeId());
    attributesRequests.add(secondAttribute.getAttributeId());
    String requestBody = new ObjectMapper().writeValueAsString(attributesRequests);

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, role, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, deleteActionType, attributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    RoleAction secondRoleAction = new RoleAction(role, deleteActionType, attributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setAttributeType(secondAttributeType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributes/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
