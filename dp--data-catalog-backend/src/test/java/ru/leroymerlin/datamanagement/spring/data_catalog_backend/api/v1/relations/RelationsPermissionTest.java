package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class RelationsPermissionTest extends MockPermissionTest {
  @Autowired
  private ResponsibilityRepository responsibilityRepository;

  @Autowired
  private RelationComponentRepository relationComponentRepository;
  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private RelationTypeRepository relationTypeRepository;

  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;

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
  Role firstAssetRole;
  Role secondAssetRole;
  RelationType firstRelationType;
  RelationTypeComponent firstRelationTypeFirstRelationTypeComponent;
  RelationTypeComponent firstRelationTypeSecondRelationTypeComponent;

  RelationType secondRelationType;
  RelationTypeComponent secondRelationTypeFirstRelationTypeComponent;
  RelationTypeComponent secondRelationTypeSecondRelationTypeComponent;
  Asset firstAsset;
  Asset secondAsset;
  Asset thirdAsset;
  Asset forthAsset;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity relationEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));
    firstAssetRole = roleRepository.save(new Role("asset role name", "desc", language, user));
    secondAssetRole = roleRepository.save(new Role("second role name", "desc", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));
    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, firstAssetRole, ResponsibleType.USER, user));
    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, secondAssetRole, ResponsibleType.USER, user));

    firstRelationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));
    firstRelationTypeFirstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first component", "desc", null, null, false, language, firstRelationType, user));
    firstRelationTypeSecondRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second component", "desc", null, null, false, language, firstRelationType, user));

    secondRelationType = relationTypeRepository.save(new RelationType("second relation type name", "desc", 2, false, false, language, user));
    secondRelationTypeFirstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second relation type first component", "desc", null, null, false, language, secondRelationType, user));
    secondRelationTypeSecondRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second relation type second component", "desc", null, null, false, language, secondRelationType, user));

    AssetType firstAssetType = assetTypeRepository.save(new AssetType("first asset type name", "desc", "acr", "color", language, user));
    AssetType secondAssetType = assetTypeRepository.save(new AssetType("second asset type name", "desc", "acr", "color", language, user));
    firstAsset = assetRepository.save(new Asset("new asset", firstAssetType, "disp", language, null, null, user));
    secondAsset = assetRepository.save(new Asset("second asset", secondAssetType, "disp", language, null, null, user));

    AssetType thirdAssetType = assetTypeRepository.save(new AssetType("third asset type name", "desc", "acr", "color", language, user));
    AssetType forthAssetType = assetTypeRepository.save(new AssetType("forth asset type name", "desc", "acr", "color", language, user));
    thirdAsset = assetRepository.save(new Asset("third asset", thirdAssetType, "disp", language, null, null, user));
    forthAsset = assetRepository.save(new Asset("forth asset", forthAssetType, "disp", language, null, null, user));

    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstRelationTypeFirstRelationTypeComponent, firstAssetType, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstRelationTypeSecondRelationTypeComponent, secondAssetType, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondRelationTypeFirstRelationTypeComponent, thirdAssetType, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondRelationTypeSecondRelationTypeComponent, forthAssetType, false, null, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    relationEntity = entityRepository.findById(UUID.fromString("2de5a051-0568-4aef-857c-8d377f8aaec1")).get();
  }

  @AfterAll
  public void clearData () {
    relationTypeComponentAssetTypeAssignmentRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    responsibilityRepository.deleteAll();
    relationComponentRepository.deleteAll();
    relationRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_relations_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationRequest> list = new ArrayList<>();
    list.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    list.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));

    PostRelationsRequest postRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationRequest> list = new ArrayList<>();
    list.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    list.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));

    PostRelationsRequest postRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relations_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationTYpe () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationRequest> list = new ArrayList<>();
    list.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    list.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));

    PostRelationsRequest postRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relations_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationTYpe () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationRequest> list = new ArrayList<>();
    list.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    list.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));

    PostRelationsRequest postRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_GlobalResponsibility_DENY_ALL_responsibility_ALLOW_ALL_assetId() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationRequest> list = new ArrayList<>();
    list.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    list.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));

    PostRelationsRequest postRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_responsibility_ALLOW_ALL_assetId_DENY_ALL_assetId() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationRequest> list = new ArrayList<>();
    list.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    list.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));

    PostRelationsRequest postRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, secondAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(secondAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_responsibility_ALLOW_ALL_assetId_DENY_ONE_ID_relationType() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationRequest> list = new ArrayList<>();
    list.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    list.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));

    PostRelationsRequest postRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relations_responsibility_DENY_ALL_assetId_ALLOW_ONE_ID_relationType() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationRequest> list = new ArrayList<>();
    list.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    list.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));

    PostRelationsRequest postRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationTYpe () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationTYpe () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType_ALLOW_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    RoleAction secondRoleAction = new RoleAction(role, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setRelationType(secondRelationType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_GlobalResponsibility_DENY_ALL_responsibility_ALLOW_ALL_assetId() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_responsibility_ALLOW_ALL_assetId_DENY_ALL_assetId() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, secondAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(secondAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_responsibility_ALLOW_ALL_assetId_DENY_ONE_ID_relationType() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_responsibility_DENY_ALL_assetId_ALLOW_ONE_ID_relationType() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_responsibility_DENY_ALL_assetId_ALLOW_ONE_ID_relationType_responsibility_ALLOW_ONE_ID_asset() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    responsibilityRepository.save(new Responsibility(user, null, thirdAsset, secondAssetRole, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(secondAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relations_bulk_responsibility_DENY_ALL_assetId_ALLOW_ONE_ID_relationType_responsibility_ALLOW_ALL_asset_DENY_ONE_ID_relation_type() throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationsRequest> relationsRequests = new ArrayList<>();

    List<PostRelationRequest> firstList = new ArrayList<>();
    firstList.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    firstList.add(new PostRelationRequest(secondAsset.getAssetId().toString(), firstRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(firstRelationType.getRelationTypeId().toString(), firstList);

    List<PostRelationRequest> secondList = new ArrayList<>();
    secondList.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), secondRelationTypeFirstRelationTypeComponent.getRelationTypeComponentId().toString()));
    secondList.add(new PostRelationRequest(forthAsset.getAssetId().toString(), secondRelationTypeSecondRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(secondRelationType.getRelationTypeId().toString(), secondList);

    relationsRequests.add(firstRequest);
    relationsRequests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(relationsRequests);

    responsibilityRepository.save(new Responsibility(user, null, thirdAsset, secondAssetRole, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(secondAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction secondRoleAction = new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    secondRoleAction.setRelationType(secondRelationType);
    roleActionRepository.save(secondRoleAction);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));
    roleActionRepository.save(new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, addActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relations_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, null, null, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relations_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, null, null, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relations_by_id_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, null, null, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relations_by_id_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, null, null, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relations_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relations_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relations_attributes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, null, null, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId() + "/attributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relations_attributes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, null, null, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId() + "/attributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relations_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, null, null, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId() + "/attributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relations_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, null, null, null, null, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId() + "/attributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_GlobalResponsibility_DENY_ALL_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_responsibility_ALLOW_ALL_assetId_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, secondAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(secondAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_responsibility_ALLOW_ALL_assetId_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_responsibility_DENY_ALL_assetId_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/" + relation.getRelationId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeFirstRelationTypeComponent, thirdAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeSecondRelationTypeComponent, forthAsset, null, null, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeFirstRelationTypeComponent, thirdAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeSecondRelationTypeComponent, forthAsset, null, null, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType_ALLOW_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeFirstRelationTypeComponent, thirdAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeSecondRelationTypeComponent, forthAsset, null, null, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    RoleAction secondRoleAction = new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setRelationType(secondRelationType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_GlobalResponsibility_DENY_ALL_responsibility_ALLOW_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeFirstRelationTypeComponent, thirdAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeSecondRelationTypeComponent, forthAsset, null, null, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_responsibility_ALLOW_ALL_assetId_DENY_ALL_assetId () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeFirstRelationTypeComponent, thirdAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeSecondRelationTypeComponent, forthAsset, null, null, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, secondAsset, secondAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(secondAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_responsibility_ALLOW_ALL_assetId_DENY_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeFirstRelationTypeComponent, thirdAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeSecondRelationTypeComponent, forthAsset, null, null, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relations_bulk_responsibility_DENY_ALL_assetId_ALLOW_ONE_ID_relationType_ALLOW_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeFirstRelationTypeComponent, firstAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(relation, firstRelationTypeSecondRelationTypeComponent, secondAsset, null, null, user));

    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeFirstRelationTypeComponent, thirdAsset, null, null, user));
    relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeSecondRelationTypeComponent, forthAsset, null, null, user));

    List<UUID> relationsIds = List.of(relation.getRelationId(), secondRelation.getRelationId());
    String requestBody = new ObjectMapper().writeValueAsString(relationsIds);

    responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstAssetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(firstAssetRole, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(firstRelationType);
    roleActionRepository.save(roleAction);

    RoleAction secondRoleAction = new RoleAction(secondAssetRole, deleteActionType, relationEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    secondRoleAction.setRelationType(secondRelationType);
    roleActionRepository.save(secondRoleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relations/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
