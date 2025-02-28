package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.AssetTypeStatusAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusAssignmentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class AssetTypeStatusesAssignmentsPermissionTest extends MockPermissionTest {
  @Autowired
  private AssetTypeStatusAssignmentRepository assetTypeStatusAssignmentRepository;

  @Autowired
  private StatusRepository statusRepository;

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
  Status status;
  AssetType assetType;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;
  ActionType grantActionType;
  ActionType revokeActionType;

  Entity assetTypeEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    status = statusRepository.save(new Status("some name", "desc", language, user));
    assetType = assetTypeRepository.save(new AssetType("asset type name", "desc", "acr", "color", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();
    grantActionType = actionTypeRepository.findById(UUID.fromString("d3bfacc2-9438-4296-a149-cfc6287e627d")).get();
    revokeActionType = actionTypeRepository.findById(UUID.fromString("5b197bb3-5140-4acf-b576-7870205b4342")).get();

    assetTypeEntity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf")).get();
  }

  @AfterAll
  public void clearData () {
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
    statusRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    assetTypeStatusAssignmentRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_assetTypeStatus_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAssetTypeStatusAssignmentRequest> statusAssignments = new ArrayList<>();
    statusAssignments.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, status.getStatusId()));

    PostAssetTypeStatusesAssignmentsRequest postRequest = new PostAssetTypeStatusesAssignmentsRequest(statusAssignments);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/status/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_assetTypeStatus_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAssetTypeStatusAssignmentRequest> statusAssignments = new ArrayList<>();
    statusAssignments.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, status.getStatusId()));

    PostAssetTypeStatusesAssignmentsRequest postRequest = new PostAssetTypeStatusesAssignmentsRequest(statusAssignments);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/status/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_assetTypeStatus_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAssetTypeStatusAssignmentRequest> statusAssignments = new ArrayList<>();
    statusAssignments.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, status.getStatusId()));

    PostAssetTypeStatusesAssignmentsRequest postRequest = new PostAssetTypeStatusesAssignmentsRequest(statusAssignments);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, assetTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/status/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_assetTypeStatus_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostAssetTypeStatusAssignmentRequest> statusAssignments = new ArrayList<>();
    statusAssignments.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, status.getStatusId()));

    PostAssetTypeStatusesAssignmentsRequest postRequest = new PostAssetTypeStatusesAssignmentsRequest(statusAssignments);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, assetTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/status/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeStatus_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetTypes/statuses",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeStatus_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetTypes/statuses",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeStatus_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/status",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeStatus_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/status",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeStatus_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/status",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeStatus_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/status",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_assetTypeStatus_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    AssetTypeStatusAssignment assignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/assetType/status/" + assignment.getAssetTypeStatusAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_assetTypeStatus_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    AssetTypeStatusAssignment assignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/assetType/status/" + assignment.getAssetTypeStatusAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_assetTypeStatus_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    AssetTypeStatusAssignment assignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, assetTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/assetType/status/" + assignment.getAssetTypeStatusAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_assetTypeStatus_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    AssetTypeStatusAssignment assignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, assetTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/assetType/status/" + assignment.getAssetTypeStatusAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
