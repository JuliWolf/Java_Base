package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.SimpleKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.CreateGlobalResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.RelationTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.UsersService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

@ExtendWith(MockitoExtension.class)
public class RoleActionCachingServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RoleActionCachingService roleActionCachingService;

  @Autowired
  private ActionTypeRepository actionTypeRepository;

  @Autowired
  private EntityRepository entityRepository;

  @Autowired
  private RoleActionRepository roleActionRepository;

  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private RolesService rolesService;

  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationTypesService relationTypesService;

  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AssetTypesService assetTypesService;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AttributeTypesService attributeTypesService;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;
  @Autowired
  private GlobalResponsibilitiesService globalResponsibilitiesService;

  @Autowired
  private UsersService usersService;

  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetsService assetsService;

  @Autowired
  private ResponsibilityRepository responsibilityRepository;

  private Role role;
  private Role userRole;
  private AttributeType attributeType;
  private RelationType relationType;
  private AssetType assetType;
  private RoleAction allPermissionsRoleAction;
  private RoleAction oneAssetTypeRoleAction;
  private RoleAction oneRelationTypeRoleAction;
  private RoleAction oneAttributeTypeRoleAction;
  private RoleAction oneRoleTypeRoleAction;
  private Asset asset;

  private GlobalResponsibility globalResponsibility;

  @BeforeEach
  public void prepareCacheData () {
    userRole = roleRepository.save(new Role("general name", "desc", language, user));
    role = roleRepository.save(new Role("user action role name", "desc", language, user));

    attributeType = attributeTypeRepository.save(new AttributeType("some name", "desc", AttributeKindType.BOOLEAN, null, null, language, user));
    relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));
    assetType = assetTypeRepository.save(new AssetType("name", "desc", "acr", "red", language, user));

    Optional<ActionType> addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Optional<ActionType> editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee"));
    Optional<ActionType> deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a"));
    Optional<ActionType> viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3"));
    Optional<Entity> assetTypeEntity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf"));
    allPermissionsRoleAction = roleActionRepository.save(new RoleAction(this.role, addActionType.get(), assetTypeEntity.get(), ActionScopeType.ALL, PermissionType.ALLOW, user));

    oneAssetTypeRoleAction = new RoleAction(this.userRole, editActionType.get(), assetTypeEntity.get(), ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    oneAssetTypeRoleAction.setAssetType(assetType);
    oneAssetTypeRoleAction = roleActionRepository.save(oneAssetTypeRoleAction);

    oneRelationTypeRoleAction = new RoleAction(this.userRole, addActionType.get(), assetTypeEntity.get(), ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    oneRelationTypeRoleAction.setRelationType(relationType);
    oneRelationTypeRoleAction = roleActionRepository.save(oneRelationTypeRoleAction);

    oneAttributeTypeRoleAction = new RoleAction(this.userRole, deleteActionType.get(), assetTypeEntity.get(), ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    oneAttributeTypeRoleAction.setAttributeType(attributeType);
    oneAttributeTypeRoleAction = roleActionRepository.save(oneAttributeTypeRoleAction);

    oneRoleTypeRoleAction = new RoleAction(this.userRole, viewActionType.get(), assetTypeEntity.get(), ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    oneRoleTypeRoleAction.setRoleType(role);
    oneRoleTypeRoleAction = roleActionRepository.save(oneRoleTypeRoleAction);

    globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, userRole, ResponsibleType.USER, user));

    asset = assetRepository.save(new Asset("asset name", assetType, "disp", language, null, null, user));

    responsibilityRepository.save(new Responsibility(user, null ,asset, userRole, ResponsibleType.USER, user));
  }

  @AfterEach
  public void clearData () {
    responsibilityRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleActionRepository.deleteAll();
    roleRepository.deleteAll();
    relationTypeRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  @Test
  public void findAllByUserIdActionTypesAndEntityNameSaveToCacheTest () {
    roleActionRepository.findAllByUserIdActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ASSET_TYPE"), 0, null, 0, null, 0, null, 0, null);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(user.getUserId().toString());

    assertAll(
      () -> assertFalse(keys.isEmpty()),
      () -> assertTrue(keys.get(0).toString().contains("[ADD, GRANT]"))
    );
  }

  @Test
  public void findAllByUserGroupsActionTypesAndEntityNameSaveToCacheTest () {
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ACTION_TYPE"), 0, null, 0, null, 0, null, 0, null);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(user.getUserId().toString());

    assertAll(
      () -> assertFalse(keys.isEmpty()),
      () -> assertTrue(keys.get(0).toString().contains("ACTION_TYPE"))
    );
  }

  @Test
  public void findAllByUserIdAndAssetIdResponsibilitiesSaveToCacheTest () {
    roleActionRepository.findAllByUserIdAndAssetIdResponsibilities(user.getUserId(), List.of(asset.getAssetId()), List.of("ADD", "GRANT"), "ACTION_TYPE", 0, null, 0, null, 0, null, 0, null);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(asset.getAssetId().toString());

    assertAll(
      () -> assertFalse(keys.isEmpty()),
      () -> assertTrue(keys.get(0).toString().contains("ACTION_TYPE"))
    );
  }

  @Test
  public void findCacheKeyByValueInListTest () {
    roleActionRepository.findAllByUserIdActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ASSET_TYPE"), 0, null, 0, null, 0, null, 0, null);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(user.getUserId().toString());

    assertAll(
      () -> assertFalse(keys.isEmpty()),
      () -> assertTrue(keys.get(0).toString().contains("ASSET_TYPE"))
    );

    roleActionCachingService.evictByRoleId(userRole.getRoleId());

    List<SimpleKey> deletedKey = roleActionCachingService.getCacheKeyBySearchString(user.getUserId().toString());
    assertTrue(deletedKey.isEmpty());
  }

  @Test
  public void clearCacheByAssetTypeIdTest () {
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ASSET_TYPE"), 0, null, 1, List.of(assetType.getAssetTypeId()), 0, null, 0, null);

    assetTypesService.deleteAssetTypeById(assetType.getAssetTypeId(), user);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(assetType.getAssetTypeId().toString());

    assertTrue(keys.isEmpty());
  }

  @Test
  public void clearCacheByAttributeTypeIdTest () {
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ATTRIBUTE_TYPE"), 0, null, 0, null, 1, List.of(attributeType.getAttributeTypeId()), 0, null);

    attributeTypesService.deleteAttributeTypeById(attributeType.getAttributeTypeId(), user);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(attributeType.getAttributeTypeId().toString());

    assertTrue(keys.isEmpty());
  }

  @Test
  public void clearCacheByRelationTypeIdTest () {
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("RELATION_TYPE"), 0, null, 0, null, 0, null, 1, List.of(relationType.getRelationTypeId()));

    relationTypesService.deleteRelationTypeById(relationType.getRelationTypeId(), user);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(relationType.getRelationTypeId().toString());

    assertTrue(keys.isEmpty());
  }

  @Test
  public void clearCacheByUserIdTest () {
    User newUser = userRepository.save(new User("some name", "first name", "last name", SourceType.KEYCLOAK, "LOOP"));
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(newUser.getUserId(), List.of("ADD", "GRANT"), List.of("RELATION_TYPE"), 0, null, 0, null, 0, null, 1, List.of(relationType.getRelationTypeId()));

    usersService.deleteUserById(newUser.getUserId(), user);
    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(newUser.getUserId().toString());

    assertTrue(keys.isEmpty());
  }

  @Test
  public void clearCacheByAssetIdTest () {
    roleActionRepository.findAllByUserIdAndAssetIdResponsibilities(user.getUserId(), List.of(asset.getAssetId()), List.of("ADD", "GRANT"), "ACTION_TYPE", 0, null, 0, null, 0, null, 0, null);

    assetsService.deleteAssetById(asset.getAssetId(), user);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(asset.getAssetId().toString());

    assertTrue(keys.isEmpty());
  }

  @Test
  public void clearCacheByRoleTypeIdTest () {
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ROLE"), 1, List.of(role.getRoleId()), 0, null, 0, null, 0, null);

    rolesService.deleteRoleById(role.getRoleId(), user);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(role.getRoleId().toString());

    assertTrue(keys.isEmpty());
  }

  @Test
  public void clearCacheByRoleIdTest () {
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ROLE"), 0, null, 0, null, 0, null, 0, null);

    rolesService.deleteRoleById(userRole.getRoleId(), user);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(role.getRoleId().toString());

    assertTrue(keys.isEmpty());
  }

  @Test
  public void clearCacheAfterCreatingGlobalResponsibilityTest () {
    globalResponsibilitiesRepository.deleteAll();

    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ROLE"), 0, null, 0, null, 0, null, 0, null);
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("GROUP"), 0, null, 0, null, 0, null, 0, null);
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("VIEW"), List.of("RESPONSIBILITY"), 0, null, 0, null, 0, null, 0, null);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(user.getUserId().toString());

    assertFalse(keys.isEmpty());

    globalResponsibilitiesService.createGlobalResponsibility(new CreateGlobalResponsibilityRequest(
      user.getUserId().toString(),
      ResponsibleType.USER,
      userRole.getRoleId().toString()
    ), user);

    List<SimpleKey> clearedKeys = roleActionCachingService.getCacheKeyBySearchString(user.getUserId().toString());

    assertTrue(clearedKeys.isEmpty());
  }

  @Test
  public void clearCacheAfterDeletingGlobalResponsibilitiesTest () {
    roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(user.getUserId(), List.of("ADD", "GRANT"), List.of("ROLE"), 0, null, 0, null, 0, null, 0, null);

    List<SimpleKey> keys = roleActionCachingService.getCacheKeyBySearchString(user.getUserId().toString());

    assertFalse(keys.isEmpty());

    globalResponsibilitiesService.deleteGlobalResponsibilityById(globalResponsibility.getGlobalResponsibilityId(), user);

    List<SimpleKey> clearedKeys = roleActionCachingService.getCacheKeyBySearchString(userRole.getRoleId().toString());

    assertTrue(clearedKeys.isEmpty());
  }
}
