package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponsibilities;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups.GroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.GlobalResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Group;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.CreateGlobalResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.PostGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */

public class GlobalResponsibilitiesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private GlobalResponsibilitiesService globalResponsibilitiesService;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private GroupRepository groupRepository;

  private Role role;
  private Group group;

  CreateGlobalResponsibilityRequest request;

  @BeforeAll
  public void prepareRoleAndGroup () {
    role = roleRepository.save(new Role("test role", "test role description", language, user));
    group = groupRepository.save(new Group("test group", "test group description", null, null , user));
  }

  @AfterAll
  public void createUserAndLanguage () {
    roleRepository.deleteAll();
    groupRepository.deleteAll();
  }

  @BeforeEach
  public void prepareValidRequest () {
    request = new CreateGlobalResponsibilityRequest(null, ResponsibleType.USER, role.getRoleId().toString());
  }

  @AfterEach
  public void clearTables() {
    globalResponsibilitiesRepository.deleteAll();
  }

  @Test
  public void createGlobalResponsibilityWrongRoleIdTest () {
    request.setRole_id("123");
    assertThrows(IllegalArgumentException.class, () -> globalResponsibilitiesService.createGlobalResponsibility(request, user));
  }

  @Test
  public void createGlobalResponsibilityOnNotExistingRoleIntegrationTest () {
    request.setRole_id(new UUID(123, 123).toString());
    assertThrows(RoleNotFoundException.class, () -> globalResponsibilitiesService.createGlobalResponsibility(request, user));
  }

  @Test
  public void createGlobalResponsibilityOnNotExistingUserIntegrationTest () {
    request.setResponsible_id(new UUID(123, 123).toString());
    assertThrows(UserNotFoundException.class, () -> globalResponsibilitiesService.createGlobalResponsibility(request, user));
  }

  @Test
  public void createGlobalResponsibilitySuccessIntegrationTest () {
    request.setResponsible_id(user.getUserId().toString());

    assertAll(
      () -> assertDoesNotThrow(() -> globalResponsibilitiesService.createGlobalResponsibility(request, user)),
      () -> assertEquals(1, globalResponsibilitiesRepository.findAll().size()),
      () -> assertEquals(request.getResponsible_id(), globalResponsibilitiesRepository.findAll().get(0).getUser().getUserId().toString())
    );
  }

  @Test
  public void getGlobalResponsibilitiesByParamsReturnEmptyListIntegrationTest () {
    request.setResponsible_id(user.getUserId().toString());
    globalResponsibilitiesService.createGlobalResponsibility(request, user);

    GetGlobalResponsibilitiesResponse response = globalResponsibilitiesService.getGlobalResponsibilitiesByParams(new UUID(123, 123), new UUID(54, 76), null, null, null, 0, 50);
    assertEquals(0, response.getResults().size());
  }

  @Test
  public void getGlobalResponsibilitiesByParamsPaginationIntegrationTest () {
    generateGlobalResponsibilities(130);

    assertAll(
      () -> assertEquals(100, globalResponsibilitiesService.getGlobalResponsibilitiesByParams(null, null, null, null, null, 0, 150).getResults().size()),
      () -> assertEquals(0, globalResponsibilitiesService.getGlobalResponsibilitiesByParams(null, null, null, null, null, 10, 50).getResults().size()),
      () -> assertEquals(130, globalResponsibilitiesService.getGlobalResponsibilitiesByParams(null, null, null, null, null, 0, 50).getTotal())
    );
  }

  @Test
  public void getGlobalResponsibilitiesByParamsIntegrationTest () {
    request.setResponsible_id(user.getUserId().toString());
    globalResponsibilitiesService.createGlobalResponsibility(request, user);

    Role role2 = roleRepository.save(new Role("role 2", "role description", language, user));
    request.setRole_id(role2.getRoleId().toString());
    globalResponsibilitiesService.createGlobalResponsibility(request, user);

    request.setResponsible_type(ResponsibleType.GROUP);
    request.setResponsible_id(group.getGroupId().toString());
    globalResponsibilitiesService.createGlobalResponsibility(request, user);

    GetGlobalResponsibilitiesResponse response = globalResponsibilitiesService.getGlobalResponsibilitiesByParams(role2.getRoleId(), null, null, null, null, 0, 50);

    assertAll(
      () -> assertEquals(2, response.getResults().size()),
      () -> assertEquals(role2.getRoleId(), response.getResults().get(0).getRole_id())
    );

    GetGlobalResponsibilitiesResponse response2 = globalResponsibilitiesService.getGlobalResponsibilitiesByParams(role2.getRoleId(), group.getGroupId(), null, null, null, 0, 50);

    assertAll(
      () -> assertEquals(1, response2.getResults().size()),
      () -> assertEquals(group.getGroupId(), response2.getResults().get(0).getResponsible_id())
    );
  }

  @Test
  public void getGlobalResponsibilityByIdWithNotExistingGlobalResponsibilityIdIntegrationTest () {
    assertThrows(GlobalResponsibilityNotFoundException.class, () -> globalResponsibilitiesService.getGlobalResponsibilityById(new UUID(123, 123)));
  }

  @Test
  public void getGlobalResponsibilityByIdSuccessIntegrationTest () {
    request.setResponsible_id(user.getUserId().toString());
    PostGlobalResponsibilityResponse globalResponsibility = globalResponsibilitiesService.createGlobalResponsibility(request, user);

    GetGlobalResponsibilityResponse response = globalResponsibilitiesService.getGlobalResponsibilityById(globalResponsibility.getGlobal_responsibility_id());

    assertAll(
      () -> assertEquals(globalResponsibility.getRole_id(), response.getRole_id()),
      () -> assertEquals(globalResponsibility.getGlobal_responsibility_id(), response.getGlobal_responsibility_id())
    );
  }

  @Test
  public void deleteGlobalResponsibilityByIdResponsibilityAlreadyDeletedIntegrationTest () {
    GlobalResponsibility globalResponsibility = new GlobalResponsibility(
      user,
      null,
      role,
      request.getResponsible_type(),
      user
    );

    globalResponsibility.setIsDeleted(true);
    GlobalResponsibility savedGlobalResponsibility = globalResponsibilitiesRepository.save(globalResponsibility);

    assertThrows(GlobalResponsibilityNotFoundException.class, () -> globalResponsibilitiesService.deleteGlobalResponsibilityById(savedGlobalResponsibility.getGlobalResponsibilityId(), user));
  }

  @Test
  public void deleteGlobalResponsibilityByIdSuccessIntegrationTest () {
    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(
      user,
      null,
      role,
      request.getResponsible_type(),
      user
    ));

    globalResponsibilitiesService.deleteGlobalResponsibilityById(globalResponsibility.getGlobalResponsibilityId(), user);

    Optional<GlobalResponsibility> foundGlobalResponsibility = globalResponsibilitiesRepository.findById(globalResponsibility.getGlobalResponsibilityId());

    assertAll(
      () -> assertTrue(foundGlobalResponsibility.get().getIsDeleted()),
      () -> assertEquals(foundGlobalResponsibility.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  private void generateGlobalResponsibilities (int count) {
    for (int i = 0; i < count; i++) {
      Role role = roleRepository.save(new Role("role_" + i, "description_" + i, language, user));
      Group group = groupRepository.save(new Group("group_" + i, "description_" + i, null, null , user));

      globalResponsibilitiesRepository.save(new GlobalResponsibility(null, group, role, ResponsibleType.GROUP, user));
    }
  }
}
