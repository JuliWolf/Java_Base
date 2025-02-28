package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.CustomViewRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PatchRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */

public class RolesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RolesService rolesService;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private EntityRepository entityRepository;
  @Autowired
  private ActionTypeRepository actionTypeRepository;
  @Autowired
  private RoleActionRepository roleActionRepository;
  @Autowired
  private CustomViewRepository customViewRepository;
  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;
  @Autowired
  private ResponsibilityRepository responsibilityRepository;
  @Autowired
  private AssetTypeCardHeaderAssignmentRepository assetTypeCardHeaderAssignmentRepository;


  @AfterEach
  public void clearTables() {
    assetTypeCardHeaderAssignmentRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    responsibilityRepository.deleteAll();
    roleActionRepository.deleteAll();
    customViewRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  public void createRoleSuccessIntegrationTest () {
    PostRoleRequest postRoleRequest = new PostRoleRequest("some role name", "some description");

    try {
      PostRoleResponse role = rolesService.createRole(postRoleRequest, user);

      assertEquals(role.getCreated_by(), user.getUserId());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createRoleRoleWithExistingNameIntegrationTest () {
    PostRoleRequest postRoleRequest = new PostRoleRequest("some role name", "some description");

    roleRepository.save(new Role(
        postRoleRequest.getRole_name(),
        postRoleRequest.getRole_description(),
        language,
        user
    ));

    try {
      assertThrows(DataIntegrityViolationException.class, () ->
          rolesService.createRole(postRoleRequest, user)
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getRoleByParamsEmptyParamsIntegrationTest () {
    String name = null;
    String description = null;

    roleRepository.save(new Role("role_1", "role_description_1", language, user));
    roleRepository.save(new Role("role_2", "role_description_2", language, user));

    assertEquals(2, rolesService.getRoleByParams(name, description, 0, 50).getResults().size());
  }

  @Test
  public void getRoleByParamsPaginationIntegrationTest () {
    generateRoles(130);

    assertAll(
      () -> assertEquals(1, rolesService.getRoleByParams(null, "110", 0, 50).getResults().size()),
      () -> assertEquals(100, rolesService.getRoleByParams(null, null, 0, 150).getResults().size()),
      () -> assertEquals(0, rolesService.getRoleByParams(null, null, 10, 50).getResults().size()),
      () -> assertEquals(130, rolesService.getRoleByParams(null, null, 0, 50).getTotal()),
      () -> assertEquals(11, rolesService.getRoleByParams("11", null, 2, 50).getTotal())
    );
  }

  @Test
  public void getRoleByParamsWithParamsIntegrationTest () {
    roleRepository.save(new Role("role_1", "role_description_1", language, user));
    roleRepository.save(new Role("role_2", "role_description_2", language, user));

    assertAll(
        () -> assertEquals(1, rolesService.getRoleByParams(null, "1", 0, 50).getResults().size()),
        () -> assertEquals(2, rolesService.getRoleByParams(null, "role", 0, 50).getResults().size()),
        () -> assertEquals(0, rolesService.getRoleByParams("1", "something", 0, 50).getResults().size())
    );
  }

  @Test
  public void getRoleByParamsUsageCountIntegrationTest () {
    Role firstRole = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    Role secondRole = roleRepository.save(new Role("role_2", "role_description_2", language, user));
    Role thirdRole = roleRepository.save(new Role("role_3", "role_description_3", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, firstRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, null, firstRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, null, secondRole, ResponsibleType.USER, user));

    assertAll(
      () -> assertEquals(2, rolesService.getRoleByParams(null, "1", 0, 50).getResults().get(0).getUsage_count()),
      () -> assertEquals(1, rolesService.getRoleByParams(null, "1", 0, 50).getResults().get(0).getResponsibilities_usage_count()),
      () -> assertEquals(1, rolesService.getRoleByParams(null, "1", 0, 50).getResults().get(0).getGlobal_responsibilities_usage_count()),
      () -> assertEquals(1, rolesService.getRoleByParams(null, "2", 0, 50).getResults().get(0).getUsage_count()),
      () -> assertEquals(1, rolesService.getRoleByParams(null, "2", 0, 50).getResults().get(0).getResponsibilities_usage_count()),
      () -> assertEquals(0, rolesService.getRoleByParams(null, "2", 0, 50).getResults().get(0).getGlobal_responsibilities_usage_count()),
      () -> assertEquals(0, rolesService.getRoleByParams(null, "3", 0, 50).getResults().get(0).getUsage_count())
    );
  }

  @Test
  public void getRoleByIdUsageCountIntegrationTest () {
    Role role1 = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    Role role2 = roleRepository.save(new Role("role_2", "role_description_2", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role1, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, null, role1, ResponsibleType.USER, user));

    assertAll(
      () -> assertEquals(2, rolesService.getRoleById(role1.getRoleId()).getUsage_count()),
      () -> assertEquals(1, rolesService.getRoleById(role1.getRoleId()).getResponsibilities_usage_count()),
      () -> assertEquals(1, rolesService.getRoleById(role1.getRoleId()).getGlobal_responsibilities_usage_count()),
      () -> assertEquals(0, rolesService.getRoleById(role2.getRoleId()).getUsage_count())
    );
  }

  @Test
  public void getRoleByIdWithExistingRoleIdIntegrationTest () {
    Role role1 = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    Role role2 = roleRepository.save(new Role("role_2", "role_description_2", language, user));

    assertAll(
        () -> assertEquals(role1.getRoleId(), rolesService.getRoleById(role1.getRoleId()).getRole_id()),
        () -> assertEquals(role2.getRoleName(), rolesService.getRoleById(role2.getRoleId()).getRole_name())
    );
  }

  @Test
  public void updateRoleSuccessIntegrationTest () {
    PatchRoleRequest roleRequest = new PatchRoleRequest(Optional.of("new role name"), Optional.empty());
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));

    PostRoleResponse updatedRole = rolesService.updateRole(role.getRoleId(), roleRequest, user);

    assertAll(
        () -> assertEquals(roleRequest.getRole_name().get(), updatedRole.getRole_name()),
        () -> assertNotEquals(role.getLastModifiedOn(), updatedRole.getLast_modified_on()),
        () -> assertNotNull(updatedRole.getLast_modified_by())
    );
  }

  @Test
  public void updateRoleRoleWithSameNameExistsIntegrationTest () {
    PatchRoleRequest roleRequest = new PatchRoleRequest(Optional.of("role_2"), Optional.empty());
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    roleRepository.save(new Role("role_2", "role_description_1", language, user));

    assertThrows(DataIntegrityViolationException.class, () -> rolesService.updateRole(role.getRoleId(), roleRequest, user));
  }

  @Test
  public void updateRoleClearRoleNameIntegrationTest () {
    // Try clear role name
    PatchRoleRequest clearRoleNameRequest = new PatchRoleRequest(Optional.empty(), Optional.empty());
    Role firtsRole = roleRepository.save(new Role("role_1", "role_description_1", language, user));

    PostRoleResponse firstResponse = rolesService.updateRole(firtsRole.getRoleId(), clearRoleNameRequest, user);

    assertEquals(firtsRole.getRoleName(), firstResponse.getRole_name());
  }

  @Test
  public void updateRoleChangeRoleNameIntegrationTest () {
    // Try change role name
    PatchRoleRequest changeRoleNameRequest = new PatchRoleRequest(Optional.of("some new name"), Optional.empty());
    Role secondRole = roleRepository.save(new Role("role_2", "role_description_1", language, user));

    PostRoleResponse secondResponse = rolesService.updateRole(secondRole.getRoleId(), changeRoleNameRequest, user);

    assertEquals("some new name", secondResponse.getRole_name());
  }

  @Test
  public void updateRoleDoNothingWithRoleNameIntegrationTest () {
    // Do nothing with name
    PatchRoleRequest doNothingWithRoleNameRequest = new PatchRoleRequest(null, Optional.empty());
    Role thirdRole = roleRepository.save(new Role("role_3", "role_description_1", language, user));

    PostRoleResponse thirdResponse = rolesService.updateRole(thirdRole.getRoleId(), doNothingWithRoleNameRequest, user);

    assertEquals(thirdRole.getRoleName(), thirdResponse.getRole_name());
  }

  @Test
  public void updateRoleClearDescriptionIntegrationTest () {
    PatchRoleRequest changeRoleNameRequest = new PatchRoleRequest(Optional.of("some new name"), Optional.empty());
    Role secondRole = roleRepository.save(new Role("role_2", "role_description_1", language, user));

    PostRoleResponse secondResponse = rolesService.updateRole(secondRole.getRoleId(), changeRoleNameRequest, user);

    assertNull(secondResponse.getRole_description());
  }

  @Test
  public void updateRoleChangeDescriptionIntegrationTest () {
    PatchRoleRequest changeRoleNameRequest = new PatchRoleRequest(Optional.of("some new name"), Optional.of("some new description"));
    Role secondRole = roleRepository.save(new Role("role_2", "role_description_1", language, user));

    PostRoleResponse secondResponse = rolesService.updateRole(secondRole.getRoleId(), changeRoleNameRequest, user);

    assertEquals("some new description", secondResponse.getRole_description());
  }

  @Test
  public void updateRoleRoleDoNothingWithDescriptionIntegrationTest () {
    PatchRoleRequest doNothingWithRoleNameRequest = new PatchRoleRequest(Optional.of("some new name"), null);
    Role thirdRole = roleRepository.save(new Role("role_3", "role_description_1", language, user));

    PostRoleResponse thirdResponse = rolesService.updateRole(thirdRole.getRoleId(), doNothingWithRoleNameRequest, user);

    assertEquals(thirdRole.getRoleDescription(), thirdResponse.getRole_description());
  }

  @Test
  public void updateRoleDoesNotExistsIntegrationTest () {
    PatchRoleRequest roleRequest = new PatchRoleRequest(Optional.of("role_2"), Optional.empty());

    assertThrows(RoleNotFoundException.class, () -> rolesService.updateRole(new UUID(123, 123), roleRequest, user));
  }

  @Test
  public void updateRoleDeletedRoleErrorIntegrationTest () {
    PatchRoleRequest roleRequest = new PatchRoleRequest(Optional.of("new role name"), Optional.empty());
    Role role = new Role("role_1", "role_description_1", language, user);
    role.setIsDeleted(true);
    Role updatedRole = roleRepository.save(role);

    assertThrows(RoleNotFoundException.class, () -> rolesService.updateRole(updatedRole.getRoleId(), roleRequest, user));
  }

  @Test
  public void deleteRoleByIdSuccessIntegrationTest () {
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));

    rolesService.deleteRoleById(role.getRoleId(), user);

    Optional<Role> foundRole = roleRepository.findById(role.getRoleId());

    assertTrue(foundRole.get().getIsDeleted());
    assertEquals(foundRole.get().getDeletedBy().getUserId(), user.getUserId());
  }

  @Test
  public void deleteRoleByIdRoleAlreadyDeletedIntegrationTest () {
    Role role = new Role("role_1", "role_description_1", language, user);
    role.setIsDeleted(true);
    Role updatedRole = roleRepository.save(role);

    assertThrows(RoleNotFoundException.class, () -> rolesService.deleteRoleById(updatedRole.getRoleId(), user));
  }

  @Test
  public void deleteRoleByIdRoleNotFoundIntegrationTest () {
    assertThrows(RoleNotFoundException.class, () -> rolesService.deleteRoleById(new UUID(123,123), user));
  }

  @Test
  public void deleteRoleByIdDeleteConnectedEntitiesIntegrationTest () {
    Optional<Entity> entity = entityRepository.findById(UUID.fromString("360ef840-5c19-424b-a86f-b3e24c2fcc2f"));
    Optional<ActionType> actionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    RoleAction roleAction = roleActionRepository.save(new RoleAction(role, actionType.get(), entity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    rolesService.deleteRoleById(role.getRoleId(), user);

    Optional<RoleAction> foundRoleAction = roleActionRepository.findById(roleAction.getRoleActionId());
    Optional<GlobalResponsibility> foundGlobalResponsibility = globalResponsibilitiesRepository.findById(globalResponsibility.getGlobalResponsibilityId());

    assertAll(
      () -> assertTrue(foundRoleAction.get().getIsDeleted()),
      () -> assertNotNull(foundRoleAction.get().getDeletedBy()),
      () -> assertEquals(user.getUserId(), foundRoleAction.get().getDeletedBy().getUserId()),
      () -> assertTrue(foundGlobalResponsibility.get().getIsDeleted()),
      () -> assertNotNull(foundGlobalResponsibility.get().getDeletedBy()),
      () -> assertEquals(user.getUserId(), foundGlobalResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRoleByIdDeleteConnectedResponsibilitiesIntegrationTest () {
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, null, role, ResponsibleType.USER, user));

    rolesService.deleteRoleById(role.getRoleId(), user);

    Optional<Responsibility> deletedResponsibility = responsibilityRepository.findById(responsibility.getResponsibilityId());
    assertAll(
      () -> assertTrue(deletedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRoleByIdDeleteConnectedAssetTypeCardHeaderAssignmentIntegrationTest () {
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));

    AssetTypeCardHeaderAssignment assignment = assetTypeCardHeaderAssignmentRepository.save(new AssetTypeCardHeaderAssignment(null, null, role, user));

    rolesService.deleteRoleById(role.getRoleId(), user);

    Optional<AssetTypeCardHeaderAssignment> deletedAssignment = assetTypeCardHeaderAssignmentRepository.findById(assignment.getAssetTypeCardHeaderAssignmentId());
    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedAssignment.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRoleByIdDeleteConnectedCustomViewsIntegrationTest () {
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    CustomView customView = customViewRepository.save(new CustomView(null, "some name", "[]", null, "query", null,  "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    rolesService.deleteRoleById(role.getRoleId(), user);

    Optional<CustomView> deletedCustomView = customViewRepository.findById(customView.getCustomViewId());

    assertAll(
      () -> assertTrue(deletedCustomView.get().getIsDeleted()),
      () -> assertNotNull(deletedCustomView.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedCustomView.get().getDeletedBy().getUserId())
    );
  }

  private void generateRoles (int count) {
    for (int i = 0; i < count; i++) {
      roleRepository.save(new Role("role_" + i, "role_description_" + i, language, user));
    }
  }
}
