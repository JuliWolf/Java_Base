package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles;

import java.net.HttpURLConnection;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class RolesPermissionTest extends MockPermissionTest {
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
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity roleEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("new role permission name", "desc", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    roleEntity = entityRepository.findById(UUID.fromString("360ef840-5c19-424b-a86f-b3e24c2fcc2f")).get();
  }

  @AfterAll
  public void clearData () {
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_roles_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRoleRequest postRequest = new PostRoleRequest("some name", "some desc");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, roleEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/roles",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    Role createdRole = roleRepository.findAll().stream().filter(item -> !item.getRoleId().equals(role.getRoleId())).findFirst().get();
    roleRepository.delete(createdRole);
  }

  @Test
  public void POST_roles_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRoleRequest postRequest = new PostRoleRequest("some name", "some desc");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, roleEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/roles",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_roles_GlobalResponsibility_ALLOW_ALL () {
    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, roleEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      "{\"role_name\": \"new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    roleRepository.delete(createdRole);
  }

  @Test
  public void PATCH_roles_GlobalResponsibility_DENY_ALL () {
    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, roleEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      "{\"role_name\": \"new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());

    roleRepository.delete(createdRole);
  }

  @Test
  public void PATCH_roles_GlobalResponsibility_ALLOW_ONE_ID_roleType () {
    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    RoleAction roleAction = new RoleAction(role, editActionType, roleEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(createdRole);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      "{\"role_name\": \"new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    roleRepository.delete(createdRole);
  }

  @Test
  public void PATCH_roles_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_roleType () {
    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, roleEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, roleEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(createdRole);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      "{\"role_name\": \"new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    roleRepository.delete(createdRole);
  }

  @Test
  public void PATCH_roles_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_roleType () {
    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, roleEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, roleEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(createdRole);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      "{\"role_name\": \"new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());

    roleRepository.delete(createdRole);
  }

  @Test
  public void GET_roles_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/roles",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_roles_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/roles",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_roles_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/roles/" + role.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_roles_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/roles/" + role.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_roles_by_id_GlobalResponsibility_ALLOW_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    RoleAction roleAction = new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/roles/" + role.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_roles_by_id_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    RoleAction roleAction = new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/roles/" + role.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_roles_by_id_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    RoleAction roleAction = new RoleAction(role, viewActionType, roleEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(role);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/roles/" + role.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_roles_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, roleEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    roleRepository.delete(createdRole);
  }

  @Test
  public void DELETE_roles_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, roleEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());

    roleRepository.delete(createdRole);
  }

  @Test
  public void DELETE_roles_GlobalResponsibility_ALLOW_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    RoleAction roleAction = new RoleAction(role, deleteActionType, roleEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRoleType(createdRole);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    roleRepository.delete(createdRole);
  }

  @Test
  public void DELETE_roles_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_roleType () {
    roleActionCachingService.clearCache();

    Role createdRole = roleRepository.save(new Role("new role name", "desc", language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, roleEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    RoleAction roleAction = new RoleAction(role, deleteActionType, roleEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRoleType(createdRole);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/roles/" + createdRole.getRoleId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());

    roleRepository.delete(createdRole);
  }
}
