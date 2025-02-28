package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userGroups.UserGroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups.GroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */

public class GroupsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private GroupsService groupsService;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserGroupRepository userGroupRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private ResponsibilityRepository responsibilityRepository;
  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @AfterEach
  public void clearTables() {
    userGroupRepository.deleteAll();
    responsibilityRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    groupRepository.deleteAll();
  }

  @Test
  public void createGroupSuccessIntegrationTest () {
    PostGroupRequest request = new PostGroupRequest("test name", "test description", "test@mail.com", "loop");

    PostGroupResponse group = groupsService.createGroup(request, user);

    assertAll(
      () -> assertEquals(request.getGroup_name(), group.getGroup_name()),
      () -> assertEquals(user.getUserId(), group.getCreated_by())
    );
  }

  @Test
  public void createGroupWithExistingNameIntegrationTest () {
    PostGroupRequest request = new PostGroupRequest("test name", "test description", "test@mail.com", "loop");

    groupRepository.save(new Group(request.getGroup_name(), "description", "email", "loop", null));

    assertThrows(DataIntegrityViolationException.class, () -> groupsService.createGroup(request, user));
  }

  @Test
  public void getGroupsByParamsPaginationIntegrationTest () {
    groupRepository.deleteAll();
    generateGroups(130);

    assertAll(
      () -> assertEquals(1, groupsService.getGroupsByParams("110", null, 0, 50).getResults().size()),
      () -> assertEquals(100, groupsService.getGroupsByParams(null, null, 0, 150).getResults().size()),
      () -> assertEquals(0, groupsService.getGroupsByParams(null, null, 10, 50).getResults().size()),
      () -> assertEquals(130, groupsService.getGroupsByParams(null, null, 0, 50).getTotal()),
      () -> assertEquals(11, groupsService.getGroupsByParams("11", null, 2, 50).getTotal())
    );
  }

  @Test
  public void getGroupsByParamsIntegrationTest () {
    groupRepository.save(new Group("group 1", "description", "email", "loop", null));
    groupRepository.save(new Group("group 2", "some data about group 2", "test2@mail.com", "slack", null));

    assertAll(
      () -> assertEquals(1, groupsService.getGroupsByParams("1", null, 0, 50).getResults().size()),
      () -> assertEquals(2, groupsService.getGroupsByParams("group", null, 0, 50).getResults().size()),
      () -> assertEquals(1, groupsService.getGroupsByParams(null, "out", 0, 50).getResults().size()),
      () -> assertEquals(2, groupsService.getGroupsByParams(null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, groupsService.getGroupsByParams("no exists", null, 0, 50).getResults().size())
    );
  }

  @Test
  public void getGroupByIdIntegrationTest () {
    Group group1 = groupRepository.save(new Group("group 1", "description", "email", "loop", null));
    Group group2 = groupRepository.save(new Group("group 2", "some data about group 2", "test2@mail.com", "slack", null));

    assertAll(
      () -> assertThrows(GroupNotFoundException.class, () -> groupsService.getGroupById(new UUID(123, 123))),
      () -> assertEquals(group1.getGroupName(), groupsService.getGroupById(group1.getGroupId()).getGroup_name()),
      () -> assertEquals(group2.getGroupDescription(), groupsService.getGroupById(group2.getGroupId()).getGroup_description())
    );
  }

  @Test
  public void deleteGroupByIdUserAlreadyDeletedIntegrationTest () {
    Group group = new Group("test group", "do something", "test@grou.com", "loop", user);
    group.setIsDeleted(true);
    groupRepository.save(group);

    assertThrows(GroupNotFoundException.class, () -> groupsService.deleteGroupById(group.getGroupId(), user));
  }

  @Test
  public void deleteGroupByIdSuccessIntegrationTest () {
    Group group = new Group("test group", "do something", "test@grou.com", "loop", user);
    groupRepository.save(group);

    groupsService.deleteGroupById(group.getGroupId(), user);
    Optional<Group> foundGroup = groupRepository.findById(group.getGroupId());

    assertAll(
      () -> assertTrue(foundGroup.get().getIsDeleted()),
      () -> assertEquals(foundGroup.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  @Test
  public void deleteGroupByIdDeleteConnectedUserGroupsIntegrationTest () {
    Group group = groupRepository.save(new Group("test group", "do something", "test@grou.com", "loop", user));
    UserGroup userGroup = userGroupRepository.save(new UserGroup(user, group, user));

    groupsService.deleteGroupById(group.getGroupId(), user);
    Optional<UserGroup> foundUserGroup = userGroupRepository.findById(userGroup.getUserGroupId());

    assertAll(
      () -> assertTrue(foundUserGroup.get().getIsDeleted()),
      () -> assertNotNull(foundUserGroup.get().getDeletedOn()),
      () -> assertEquals(foundUserGroup.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  @Test
  public void deleteGroupByIdDeleteConnectedResponsibilitiesIntegrationTest () {
    Group group = groupRepository.save(new Group("test group", "do something", "test@grou.com", "loop", user));
    Role role = roleRepository.save(new Role( "role name", "some desc", null, user));
    Responsibility responsibility = responsibilityRepository.save(new Responsibility(null, group, null, role, ResponsibleType.GROUP, user));

    groupsService.deleteGroupById(group.getGroupId(), user);

    Optional<Responsibility> deletedResponsibility = responsibilityRepository.findById(responsibility.getResponsibilityId());
    assertAll(
      () -> assertTrue(deletedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteGroupByIdDeleteConnectedGlobalResponsibilitiesIntegrationTest () {
    Group group = groupRepository.save(new Group("test group", "do something", "test@grou.com", "loop", user));
    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, null, ResponsibleType.GROUP, user));

    groupsService.deleteGroupById(group.getGroupId(), user);

    Optional<GlobalResponsibility> deletedGlobalResponsibility = globalResponsibilitiesRepository.findById(globalResponsibility.getGlobalResponsibilityId());
    assertAll(
      () -> assertTrue(deletedGlobalResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedGlobalResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedGlobalResponsibility.get().getDeletedBy().getUserId())
    );
  }

  private void generateGroups (int count) {
    for (int i = 0; i < count; i++) {
      groupRepository.save(new Group("group_" + i, "description_" + i, "test@group.com", "loop", user));
    }
  }
}
