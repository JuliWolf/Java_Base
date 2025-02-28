package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.RoleActionsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities.EntityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions.RoleActionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostPrivilegeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class RoleActionsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RoleActionsService roleActionsService;

  @Autowired
  private RoleActionRepository roleActionRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private ActionTypeRepository actionTypeRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private EntityRepository entityRepository;
  @Autowired
  private RoleRepository roleRepository;

  private Role role;

  List<PostPrivilegeRequest> allowedPrivilegesRequest;
  List<PostPrivilegeRequest> notAllowedPrivilegesRequest;
  PostRoleActionsRequest request;

  @BeforeAll
  public void createRole () {
    role = roleRepository.save(new Role(
      "test role",
      "test role description",
      language,
      user
    ));
  }

  @AfterAll
  public void clearRoleTable () {
    roleRepository.deleteAll();
  }

  @BeforeEach
  public void prepareValidRequest () {
    allowedPrivilegesRequest = Arrays.asList(
      new PostPrivilegeRequest("54d4330c-d08f-4fb2-b706-2ec3c022286d", "b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf", ActionScopeType.ALL, null, false),
      new PostPrivilegeRequest("3b086c0e-d19e-4874-9c18-8e9daf952bee", "c4dbe5b6-98dc-431e-807f-a5109a19ee9d", ActionScopeType.ALL, null, false),
      new PostPrivilegeRequest("b8eb41e4-e43f-4259-a198-81fc19e7e90a", "360ef840-5c19-424b-a86f-b3e24c2fcc2f", ActionScopeType.ALL, null, false)
    );

    notAllowedPrivilegesRequest = Arrays.asList(
      new PostPrivilegeRequest("c7f65d90-b311-4fac-9af3-68289a60e7e3", "360ef840-5c19-424b-a86f-b3e24c2fcc2f", ActionScopeType.ALL, null, false),
      new PostPrivilegeRequest("c7f65d90-b311-4fac-9af3-68289a60e7e3", "b1c5eb66-b777-4ee8-8a39-6ad9e43f447f", ActionScopeType.ALL, null, false),
      new PostPrivilegeRequest("b8eb41e4-e43f-4259-a198-81fc19e7e90a", "c4dbe5b6-98dc-431e-807f-a5109a19ee9d", ActionScopeType.ALL, null, false)
    );

    request = new PostRoleActionsRequest(role.getRoleId().toString(), allowedPrivilegesRequest, notAllowedPrivilegesRequest);
  }

  @AfterEach
  public void clearTables() {
    assetTypeRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void createRoleActionsWrongRoleIdTest () {
    PostRoleActionsRequest request = new PostRoleActionsRequest("123", allowedPrivilegesRequest, notAllowedPrivilegesRequest);
    assertThrows(IllegalArgumentException.class, () -> roleActionsService.createRoleActions(request, user));
  }

  @Test
  public void createRoleActionsOnNotExistingRoleIntegrationTest () {
    PostRoleActionsRequest request = new PostRoleActionsRequest(new UUID(123,123).toString(), allowedPrivilegesRequest, notAllowedPrivilegesRequest);
    assertThrows(RoleNotFoundException.class, () -> roleActionsService.createRoleActions(request, user));
  }

  @Test
  public void createRoleActionsOnNotExistingEntityIntegrationTest () {
    PostPrivilegeRequest postPrivilegeRequest = allowedPrivilegesRequest.get(1);
    postPrivilegeRequest.setEntity_type_id(new UUID(123, 5645).toString());
    allowedPrivilegesRequest.set(1, postPrivilegeRequest);
    assertThrows(EntityNotFoundException.class, () -> roleActionsService.createRoleActions(request, user));
  }

  @Test
  public void createRoleActionsAllActionScopeSuccessIntegrationTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> roleActionsService.createRoleActions(request, user)),
      () -> assertEquals(6, roleActionRepository.findAll().size()),
      () -> assertEquals(request.getRole_id(), roleActionRepository.findAll().get(0).getRole().getRoleId().toString())
    );
  }

  @Test
  public void createRoleActionsCreateMultipleOneWithDifferentObjectIdsIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("Database", "database description", "db", "orange", language, user));
    AssetType secondAssetType = assetTypeRepository.save(new AssetType("Another asset type", "database description", "db", "orange", language, user));
    List<PostPrivilegeRequest> allowedPrivileges = Arrays.asList(
      new PostPrivilegeRequest("54d4330c-d08f-4fb2-b706-2ec3c022286d", "f2d482f5-fe31-45c8-856d-512a9aa56fde", ActionScopeType.ONE_ID, assetType.getAssetTypeId().toString(), false),
      new PostPrivilegeRequest("b8eb41e4-e43f-4259-a198-81fc19e7e90a", "f2d482f5-fe31-45c8-856d-512a9aa56fde", ActionScopeType.ONE_ID, assetType.getAssetTypeId().toString(), false),
      new PostPrivilegeRequest("c7f65d90-b311-4fac-9af3-68289a60e7e3", "f2d482f5-fe31-45c8-856d-512a9aa56fde", ActionScopeType.ONE_ID, secondAssetType.getAssetTypeId().toString(), false)
    );

    request.setPrivilege_allowed(allowedPrivileges);
    request.setPrivilege_not_allowed(new ArrayList<>());

    assertDoesNotThrow(() -> roleActionsService.createRoleActions(request, user));
  }

  @Test
  public void createRoleActionsOneIdActionScopeCreateAndDeleteAssetIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("Database", "database description", "db", "orange", language, user));
    List<PostPrivilegeRequest> privilegeAllowed = request.getPrivilege_allowed();
    PostPrivilegeRequest postPrivilegeRequest = privilegeAllowed.get(1);
    postPrivilegeRequest.setEntity_type_id("f2d482f5-fe31-45c8-856d-512a9aa56fde");
    postPrivilegeRequest.setObject_type_id(assetType.getAssetTypeId().toString());
    postPrivilegeRequest.setAction_scope(ActionScopeType.ONE_ID);

    request.setPrivilege_allowed(List.of(postPrivilegeRequest));
    request.setPrivilege_not_allowed(new ArrayList<>());
    PostRoleActionsResponse response = roleActionsService.createRoleActions(request, user);

    assertAll(
      () -> assertDoesNotThrow(() -> response),
      () -> assertEquals(assetType.getAssetTypeId(), response.getPrivilege_allowed().get(0).getObject_type_id()),
      () -> assertEquals(assetType.getAssetTypeId(), roleActionRepository.findAll().get(0).getAssetType().getAssetTypeId())
    );

    assetTypeRepository.delete(assetType);

    assertEquals(0, roleActionRepository.findAll().size());
  }

  @Test
  public void createRoleActionsOneIdActionScopeCreateAndDeleteAttributeTypeIntegrationTest () {
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.DECIMAL, null, null,language, user));
    List<PostPrivilegeRequest> privilegeAllowed = request.getPrivilege_allowed();
    PostPrivilegeRequest postPrivilegeRequest = privilegeAllowed.get(1);
    postPrivilegeRequest.setEntity_type_id("c4dbe5b6-98dc-431e-807f-a5109a19ee9d");
    postPrivilegeRequest.setObject_type_id(attributeType.getAttributeTypeId().toString());
    postPrivilegeRequest.setAction_scope(ActionScopeType.ONE_ID);

    request.setPrivilege_allowed(List.of(postPrivilegeRequest));
    request.setPrivilege_not_allowed(new ArrayList<>());

    assertAll(
      () -> assertDoesNotThrow(() -> roleActionsService.createRoleActions(request, user)),
      () -> assertEquals(attributeType.getAttributeTypeId(), roleActionRepository.findAll().get(0).getAttributeType().getAttributeTypeId())
    );

    attributeTypeRepository.delete(attributeType);

    assertEquals(0, roleActionRepository.findAll().size());
  }

  @Test
  public void createRoleActionsChangeActionScopeOneIdToAllIntegrationTest () {
    Optional<ActionType> addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Optional<Entity> assetTypeEntity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf"));

    RoleAction oneIdRoleAction = roleActionRepository.save(new RoleAction(
      role,
      addActionType.get(),
      assetTypeEntity.get(),
      ActionScopeType.ONE_ID,
      PermissionType.ALLOW,
      user
    ));

    List<PostPrivilegeRequest> allowedPrivileges = allowedPrivilegesRequest = List.of(
      new PostPrivilegeRequest("54d4330c-d08f-4fb2-b706-2ec3c022286d", "b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf", ActionScopeType.ALL, null, false)
    );

    request = new PostRoleActionsRequest(role.getRoleId().toString(), allowedPrivileges, new ArrayList<>());
    roleActionsService.createRoleActions(request, user);

    Optional<RoleAction> deletedOneIdRoleAction = roleActionRepository.findById(oneIdRoleAction.getRoleActionId());

    assertAll(
      () -> assertTrue(deletedOneIdRoleAction.get().getIsDeleted()),
      () -> assertNotNull(deletedOneIdRoleAction.get().getDeletedOn()),
      () -> assertEquals(deletedOneIdRoleAction.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  @Test
  public void getRoleActionsByParamsIntegrationTest () {
    Optional<ActionType> addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Optional<ActionType> viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3"));
    Optional<ActionType> revokeActionType = actionTypeRepository.findById(UUID.fromString("5b197bb3-5140-4acf-b576-7870205b4342"));

    Optional<Entity> assetEntity = entityRepository.findById(UUID.fromString("f2d482f5-fe31-45c8-856d-512a9aa56fde"));
    Optional<Entity> assetTypeEntity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf"));
    Optional<Entity> attributeTypeEntity = entityRepository.findById(UUID.fromString("c4dbe5b6-98dc-431e-807f-a5109a19ee9d"));
    Optional<Entity> responsibilityGlobalEntity = entityRepository.findById(UUID.fromString("c7c5febf-6712-449c-a5a4-97e1da4a104a"));

    AssetType assetType = assetTypeRepository.save(new AssetType("Database 2 ", "database description", "db", "orange", language, user));
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("some name 2", "some description", AttributeKindType.DECIMAL, null, null,null, null));

    Role secondRole = roleRepository.save(new Role("test role 2","test role description", language, user));

    roleActionRepository.save(new RoleAction(role, addActionType.get(), assetEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, viewActionType.get(), assetTypeEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(secondRole, revokeActionType.get(), attributeTypeEntity.get(), ActionScopeType.ALL, PermissionType.DENY, user));
    roleActionRepository.save(new RoleAction(secondRole, addActionType.get(), responsibilityGlobalEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, viewActionType.get(), attributeTypeEntity.get(), ActionScopeType.ALL, PermissionType.DENY, user));

    roleActionRepository.save(new RoleAction(secondRole, addActionType.get(), assetEntity.get(), assetType, null, null, null, ActionScopeType.ONE_ID, PermissionType.DENY, user));
    roleActionRepository.save(new RoleAction(role, viewActionType.get(), responsibilityGlobalEntity.get(), null, role, null, null, ActionScopeType.ONE_ID, PermissionType.DENY, user));
    roleActionRepository.save(new RoleAction(role, viewActionType.get(), attributeTypeEntity.get(), null, null, attributeType, null, ActionScopeType.ONE_ID, PermissionType.DENY, user));

    assertAll(
      () -> assertEquals(8, roleActionsService.getRoleActionsByParams(null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(5, roleActionsService.getRoleActionsByParams(role.getRoleId(), null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, roleActionsService.getRoleActionsByParams(secondRole.getRoleId(), null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, roleActionsService.getRoleActionsByParams(secondRole.getRoleId(), assetTypeEntity.get().getId(), null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, roleActionsService.getRoleActionsByParams(secondRole.getRoleId(), assetEntity.get().getId(), null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(2, roleActionsService.getRoleActionsByParams(null, assetEntity.get().getId(), null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, roleActionsService.getRoleActionsByParams(null, null, null, null, null, assetType.getAssetTypeId(), 0, 50).getResults().size()),
      () -> assertEquals(1, roleActionsService.getRoleActionsByParams(null, null, null, null, null, attributeType.getAttributeTypeId(), 0, 50).getResults().size()),
      () -> assertEquals(0, roleActionsService.getRoleActionsByParams(null, null, null, null, PermissionType.ALLOW, attributeType.getAttributeTypeId(), 0, 50).getResults().size()),
      () -> assertEquals(5, roleActionsService.getRoleActionsByParams(null, null, null, ActionScopeType.ALL, null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, roleActionsService.getRoleActionsByParams(null, null, null, ActionScopeType.ONE_ID, null, null, 0, 50).getResults().size())
    );
  }

  @Test
  public void getRoleActionsByParamsPaginationIntegrationTest () {
    generateRoleActions(130);

    assertAll(
      () -> assertEquals(50, roleActionsService.getRoleActionsByParams(null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, roleActionsService.getRoleActionsByParams(null, null, null, null, null, null, 3, 50).getResults().size()),
      () -> assertEquals(100, roleActionsService.getRoleActionsByParams(null, null, null, null, null, null, 0, 140).getResults().size()),
      () -> assertEquals(130, roleActionsService.getRoleActionsByParams(null, null, null, null, null, null, 0, 50).getTotal())
    );
  }

  @Test
  public void deleteRoleActionByIdRoleActionNotFoundIntegrationTest () {
    assertThrows(RoleActionNotFoundException.class, () -> roleActionsService.deleteRoleActionById(new UUID(123,123), user));
  }

  @Test
  public void deleteRoleActionByIdSuccessIntegrationTest () {
    Optional<ActionType> actionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Optional<Entity> entity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf"));
    RoleAction role = roleActionRepository.save(new RoleAction(this.role, actionType.get(), entity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));

    roleActionsService.deleteRoleActionById(role.getRoleActionId(), user);

    Optional<RoleAction> foundRoleAction = roleActionRepository.findById(role.getRoleActionId());

    assertAll(
      () -> assertTrue(foundRoleAction.get().getIsDeleted()),
      () -> assertEquals(foundRoleAction.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  @Test
  public void deleteRoleActionByIdRoleActionAlreadyDeletedIntegrationTest () {
    Optional<ActionType> actionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Optional<Entity> entity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf"));
    RoleAction roleAction = new RoleAction(this.role, actionType.get(), entity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user);
    roleAction.setIsDeleted(true);
    RoleAction updatedRoleAction = roleActionRepository.save(roleAction);

    assertThrows(RoleActionNotFoundException.class, () -> roleActionsService.deleteRoleActionById(updatedRoleAction.getRoleActionId(), user));
  }

  @Test
  public void deleteRoleActionByIdDeleteAllDenyIfAllowAllRoleActionDeletedIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("Database", "database description", "db", "orange", language, user));

    Optional<ActionType> actionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Optional<Entity> entity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf"));

    RoleAction allIdRoleAction = roleActionRepository.save(new RoleAction(
      role,
      actionType.get(),
      entity.get(),
      assetType,
      null,
      null,
      null,
      ActionScopeType.ONE_ID,
      PermissionType.DENY,
      user
    ));

    RoleAction roleAction = roleActionRepository.save(new RoleAction(this.role, actionType.get(), entity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));

    roleActionsService.deleteRoleActionById(roleAction.getRoleActionId(), user);

    Optional<RoleAction> deletedOneIdRoleAction = roleActionRepository.findById(allIdRoleAction.getRoleActionId());

    assertAll(
      () -> assertTrue(deletedOneIdRoleAction.get().getIsDeleted()),
      () -> assertNotNull(deletedOneIdRoleAction.get().getDeletedOn()),
      () -> assertEquals(deletedOneIdRoleAction.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  private void generateRoleActions (int count) {
    Optional<ActionType> addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Optional<Entity> assetEntity = entityRepository.findById(UUID.fromString("f2d482f5-fe31-45c8-856d-512a9aa56fde"));

    for (int i = 0; i < count; i++) {
      Role role = roleRepository.save(new Role("role name_"+i, "desc", language, user));

      roleActionRepository.save(new RoleAction(role, addActionType.get(), assetEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));
    }
  }
}
