package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostOrPatchUserRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class UsersPermissionTest extends MockPermissionTest {

  @Autowired
  private UserRepository userRepository;

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

  Entity userEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("role name for user permission test", "desc", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    userEntity = entityRepository.findById(UUID.fromString("004ba3ec-2b19-482b-b39d-fcf3f6a58e1c")).get();
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
  public void POST_users_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostOrPatchUserRequest postRequest = new PostOrPatchUserRequest("some new username", "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, userEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/users",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    User createdUser = userRepository.findAll().stream().filter(userObj -> !userObj.getUserId().equals(user.getUserId())).findFirst().get();
    userRepository.delete(createdUser);
  }

  @Test
  public void POST_users_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostOrPatchUserRequest postRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, userEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/users",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_users_bulk_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("first new username", "first name", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("second username", "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    requests.add(firstRequest);
    requests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(requests);

    roleActionRepository.save(new RoleAction(role, addActionType, userEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/users/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    userRepository.findAll().stream().filter(userObj -> !userObj.getUserId().equals(user.getUserId())).forEach(user -> {
      userRepository.delete(user);
    });
  }

  @Test
  public void POST_users_bulk_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("first new username", "first name", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("second username", "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    requests.add(firstRequest);
    requests.add(secondRequest);
    String requestBody = new ObjectMapper().writeValueAsString(requests);

    roleActionRepository.save(new RoleAction(role, addActionType, userEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/users/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_users_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();
    User newUser = userRepository.save(new User("username new", "first name", "second name", SourceType.KEYCLOAK, "test@test.com"));

    PostOrPatchUserRequest postRequest = new PostOrPatchUserRequest("username", null, null, null, null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, userEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/users/" + newUser.getUserId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    userRepository.delete(newUser);
  }

  @Test
  public void PATCH_users_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    User newUser = userRepository.save(new User("username new", "first name", "second name", SourceType.KEYCLOAK, "test@test.com"));

    PostOrPatchUserRequest postRequest = new PostOrPatchUserRequest("username", null, null, null, null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, userEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/users/" + newUser.getUserId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
    userRepository.delete(newUser);
  }

  @Test
  public void GET_users_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, userEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/users",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_users_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, userEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/users",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_users_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    User newUser = userRepository.save(new User("username new", "first name", "second name", SourceType.KEYCLOAK, "test@test.com"));

    roleActionRepository.save(new RoleAction(role, viewActionType, userEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/users/" + newUser.getUserId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    userRepository.delete(newUser);
  }

  @Test
  public void GET_users_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    User newUser = userRepository.save(new User("username new", "first name", "second name", SourceType.KEYCLOAK, "test@test.com"));

    roleActionRepository.save(new RoleAction(role, viewActionType, userEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/users/" + newUser.getUserId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());

    userRepository.delete(newUser);
  }

  @Test
  public void GET_users_roles_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    User newUser = userRepository.save(new User("username new", "first name", "second name", SourceType.KEYCLOAK, "test@test.com"));

    roleActionRepository.save(new RoleAction(role, viewActionType, userEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/users/" + newUser.getUserId() + "/roles",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    userRepository.delete(newUser);
  }

  @Test
  public void GET_users_roles_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    User newUser = userRepository.save(new User("username new", "first name", "second name", SourceType.KEYCLOAK, "test@test.com"));

    roleActionRepository.save(new RoleAction(role, viewActionType, userEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/users/" + newUser.getUserId() + "/roles",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());

    userRepository.delete(newUser);
  }

  @Test
  public void DELETE_users_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    User newUser = userRepository.save(new User("username new", "first name", "second name", SourceType.KEYCLOAK, "test@test.com"));

    roleActionRepository.save(new RoleAction(role, deleteActionType, userEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/users/" + newUser.getUserId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());

    userRepository.delete(newUser);
  }

  @Test
  public void DELETE_users_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    User newUser = userRepository.save(new User("username new", "first name", "second name", SourceType.KEYCLOAK, "test@test.com"));

    roleActionRepository.save(new RoleAction(role, deleteActionType, userEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/users/" + newUser.getUserId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());

    userRepository.delete(newUser);
  }
}
