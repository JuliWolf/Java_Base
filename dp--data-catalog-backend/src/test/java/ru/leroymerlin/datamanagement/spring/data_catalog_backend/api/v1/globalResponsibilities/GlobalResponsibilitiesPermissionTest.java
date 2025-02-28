package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponsibilities;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups.GroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.CreateGlobalResponsibilityRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class GlobalResponsibilitiesPermissionTest extends MockPermissionTest {
  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @Autowired
  private GroupRepository groupRepository;

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
  Group group;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;
  ActionType grantActionType;
  ActionType revokeActionType;

  Entity responsibilityGlobalEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    group = groupRepository.save(new Group("group name", "desc", "some email", "messenger", user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();
    grantActionType = actionTypeRepository.findById(UUID.fromString("d3bfacc2-9438-4296-a149-cfc6287e627d")).get();
    revokeActionType = actionTypeRepository.findById(UUID.fromString("5b197bb3-5140-4acf-b576-7870205b4342")).get();

    responsibilityGlobalEntity = entityRepository.findById(UUID.fromString("c7c5febf-6712-449c-a5a4-97e1da4a104a")).get();
  }

  @BeforeEach
  public void prepareResponsibilities () {
    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));
  }

  @AfterAll
  public void clearData () {
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
    groupRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    globalResponsibilitiesRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_globalResponsibilities_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    CreateGlobalResponsibilityRequest postRequest = new CreateGlobalResponsibilityRequest(group.getGroupId().toString(), ResponsibleType.GROUP, role.getRoleId().toString());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/globalResponsibilities",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_globalResponsibilities_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    CreateGlobalResponsibilityRequest postRequest = new CreateGlobalResponsibilityRequest(group.getGroupId().toString(), ResponsibleType.GROUP, role.getRoleId().toString());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/globalResponsibilities",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_globalResponsibilities_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_roleType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    CreateGlobalResponsibilityRequest postRequest = new CreateGlobalResponsibilityRequest(group.getGroupId().toString(), ResponsibleType.GROUP, role.getRoleId().toString());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, grantActionType, responsibilityGlobalEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/globalResponsibilities",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_globalResponsibilities_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_roleType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    CreateGlobalResponsibilityRequest postRequest = new CreateGlobalResponsibilityRequest(group.getGroupId().toString(), ResponsibleType.GROUP, role.getRoleId().toString());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, grantActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, grantActionType, responsibilityGlobalEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/globalResponsibilities",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_globalResponsibilities_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/globalResponsibilities",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_globalResponsibilities_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/globalResponsibilities",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_globalResponsibilities_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/globalResponsibilities/" + globalResponsibility.getGlobalResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_globalResponsibilities_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/globalResponsibilities/" + globalResponsibility.getGlobalResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_globalResponsibilities_by_id_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, responsibilityGlobalEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/globalResponsibilities/" + globalResponsibility.getGlobalResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_globalResponsibilities_by_id_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, responsibilityGlobalEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/globalResponsibilities/" + globalResponsibility.getGlobalResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_globalResponsibilities_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));

    roleActionRepository.save(new RoleAction(role, revokeActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/globalResponsibilities/" + globalResponsibility.getGlobalResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_globalResponsibilities_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));

    roleActionRepository.save(new RoleAction(role, revokeActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/globalResponsibilities/" + globalResponsibility.getGlobalResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_globalResponsibilities_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));

    roleActionRepository.save(new RoleAction(role, revokeActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, revokeActionType, responsibilityGlobalEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/globalResponsibilities/" + globalResponsibility.getGlobalResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_globalResponsibilities_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));

    roleActionRepository.save(new RoleAction(role, revokeActionType, responsibilityGlobalEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, revokeActionType, responsibilityGlobalEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/globalResponsibilities/" + globalResponsibility.getGlobalResponsibilityId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
