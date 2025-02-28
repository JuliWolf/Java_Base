package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.UserGroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.UserGroupsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups.GroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userGroups.UserGroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Group;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.UserGroup;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.UserGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.get.GetUserGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.post.PostUserGroupRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class UserGroupsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private UserGroupsService userGroupsService;

  @Autowired
  private UserGroupRepository userGroupRepository;

  @Autowired
  private GroupRepository groupRepository;

  private Group group;

  @BeforeAll
  public void prepareGroup () {
    group = groupRepository.save(new Group("group name", "group description", "group@mail.com", "loop", user));
  }

  @AfterAll
  public void clearGroup () {
    groupRepository.deleteAll();
  }

  @AfterEach
  public void clearTables() {
    userGroupRepository.deleteAll();
  }

  @Test
  public void createUserGroupIllegalUserIdIntegrationTest () {
    try {
      assertAll(
        () -> assertThrows(IllegalArgumentException.class, () -> userGroupsService.createUserGroup(new PostUserGroupRequest("123", group.getGroupId().toString()), user)),
        () -> assertThrows(IllegalArgumentException.class, () -> userGroupsService.createUserGroup(new PostUserGroupRequest(user.getUserId().toString(), "123"), user))
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createUserGroupSuccessIntegrationTest () {
    try {
      PostUserGroupRequest request = new PostUserGroupRequest(user.getUserId().toString(), group.getGroupId().toString());
      UserGroupResponse response = userGroupsService.createUserGroup(request, user);

      assertAll(
        () -> assertEquals(request.getGroup_id(), response.getGroup_id().toString()),
        () -> assertEquals(user.getUsername(), response.getUsername())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createUserGroupWithExistingGroupIdAndRoleIdIntegrationTest () {
    try {
      PostUserGroupRequest request = new PostUserGroupRequest(user.getUserId().toString(), group.getGroupId().toString());
      userGroupsService.createUserGroup(request, user);

      assertThrows(DataIntegrityViolationException.class, () -> userGroupsService.createUserGroup(request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getUserGroupsByUserIdAndRoleIdEmptyListIntegrationTest () {
    GetUserGroupsResponse response = userGroupsService.getUserGroupsByUserIdAndRoleId(new UUID(123, 123), null, 0, 50);

    assertEquals(0, response.getResults().size());
  }

  @Test
  public void getUserGroupsByUserIdAndRoleIdIntegrationTest () {
    Group group1 = groupRepository.save(new Group("test group", "test description", "test@mail.com", "loop", user));
    User user1 = new User("username", "firstname", "secondname", SourceType.KEYCLOAK, "test@mail.com");
    user1.setLanguage(language);
    user1 = userRepository.save(user1);

    UserGroup userGroupWithUserAndGroup = userGroupRepository.save(new UserGroup(user, group, user));
    userGroupRepository.save(new UserGroup(user, group1, user));
    userGroupRepository.save(new UserGroup(user1, group, user));

    GetUserGroupsResponse userGroupWithUserAndGroupResponse = userGroupsService.getUserGroupsByUserIdAndRoleId(user.getUserId(), group.getGroupId(), 0, 50);
    assertAll(
      () -> assertEquals(1, userGroupWithUserAndGroupResponse.getResults().size()),
      () -> assertEquals(user.getUserId(), userGroupWithUserAndGroupResponse.getResults().get(0).getUser_id()),
      () -> assertEquals(group.getGroupId(), userGroupWithUserAndGroupResponse.getResults().get(0).getGroup_id()),
      () -> assertEquals(userGroupWithUserAndGroup.getUserGroupId(), userGroupWithUserAndGroupResponse.getResults().get(0).getUser_group_id())
    );

    GetUserGroupsResponse userGroupWithUserResponse = userGroupsService.getUserGroupsByUserIdAndRoleId(user.getUserId(), null, 0, 50);
    assertAll(
      () -> assertEquals(2, userGroupWithUserResponse.getResults().size()),
      () -> assertEquals(user.getUserId(), userGroupWithUserResponse.getResults().get(0).getUser_id())
    );

    GetUserGroupsResponse allUserGroupsResponse = userGroupsService.getUserGroupsByUserIdAndRoleId(null, null, 0, 50);
    assertEquals(3, allUserGroupsResponse.getResults().size());
  }

  @Test
  public void getUserGroupsByUserIdAndRoleIdPaginationIntegrationTest () {
    generateUseGroups(130);

    assertAll(
      () -> assertEquals(100, userGroupsService.getUserGroupsByUserIdAndRoleId(null, null, 0, 150).getResults().size()),
      () -> assertEquals(0, userGroupsService.getUserGroupsByUserIdAndRoleId(null, null, 10, 50).getResults().size()),
      () -> assertEquals(130, userGroupsService.getUserGroupsByUserIdAndRoleId(null, null, 0, 50).getTotal())
    );
  }

  @Test
  public void deleteUserGroupByIdSuccessIntegrationTest () {
    UserGroup userGroupWithUserAndGroup = userGroupRepository.save(new UserGroup(user, group, user));

    userGroupsService.deleteUserGroupById(userGroupWithUserAndGroup.getUserGroupId(), user);

    Optional<UserGroup> userGroup = userGroupRepository.findById(userGroupWithUserAndGroup.getUserGroupId());

    assertAll(
      () -> assertTrue(userGroup.get().getIsDeleted()),
      () -> assertNotNull(userGroup.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), userGroup.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteUserGroupByIdUserGroupAlreadyDeletedIntegrationTest () {
    UserGroup userGroupWithUserAndGroup = new UserGroup(user, group, user);
    userGroupWithUserAndGroup.setIsDeleted(true);
    userGroupRepository.save(userGroupWithUserAndGroup);

    assertThrows(UserGroupNotFoundException.class, () -> userGroupsService.deleteUserGroupById(userGroupWithUserAndGroup.getUserGroupId(), user));
  }

  @Test
  public void deleteUserGroupByIdUserGroupNotFoundIntegrationTest () {
    assertThrows(UserGroupNotFoundException.class, () -> userGroupsService.deleteUserGroupById(UUID.randomUUID(), user));
  }

  private void generateUseGroups (int count) {
    for (int i = 0; i < count; i++) {
      User user = userRepository.save(new User("username_" + i, "fist_name_" + i, "last_name_" + i, SourceType.KEYCLOAK, "test_" + i + "@email.com"));
      Group group = groupRepository.save(new Group("groupname_" + i, "description_" + i, "group_" + i + "@greoup.com", "loop", this.user));
      userGroupRepository.save(new UserGroup(user, group, this.user));
    }
  }
}
