package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.*;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userGroups.UserGroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.post.PostUserGroupRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class UserGroupsPermissionTest extends MockPermissionTest {

  @Autowired
  private UserGroupRepository userGroupRepository;

  @Autowired
  private GroupRepository groupRepository;

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
  Group group;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity userGroupEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    group = groupRepository.save(new Group("test name", "desc", "user@test.com", "messenger", user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    userGroupEntity = entityRepository.findById(UUID.fromString("7111c0aa-ed81-45d9-8f59-155e9e5f2788")).get();
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
    roleActionRepository.deleteAll();
    userGroupRepository.deleteAll();
  }

  @Test
  public void POST_userGroups_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostUserGroupRequest postRequest = new PostUserGroupRequest(user.getUserId().toString(), group.getGroupId().toString());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, userGroupEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/userGroups",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_userGroups_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostUserGroupRequest postRequest = new PostUserGroupRequest(user.getUserId().toString(), group.getGroupId().toString());
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, userGroupEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/userGroups",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_userGroups_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, userGroupEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/userGroups",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_userGroups_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, userGroupEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/userGroups",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_userGroups_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    UserGroup userGroup = userGroupRepository.save(new UserGroup(user, group, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, userGroupEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/userGroups/" + userGroup.getUserGroupId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_userGroups_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    UserGroup userGroup = userGroupRepository.save(new UserGroup(user, group, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, userGroupEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/userGroups/" + userGroup.getUserGroupId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }
}
