package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes.ActionTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes.ActionTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities.EntitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities.EntityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.RelationTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions.RoleActionObjectNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.get.GetRoleActionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.get.GetRoleActionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostPrivilegeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostPrivilegeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;

/**
 * @author JuliWolf
 */
@Service
public class RoleActionsServiceImpl extends RoleActionsDAO implements RoleActionsService {
  private final List<EntityNameType> OWNER_USER_ID_ENTITIES = List.of(EntityNameType.SUBSCRIPTION);

  @Autowired
  private RoleActionCachingService roleActionCachingService;

  @Autowired
  private EntitiesDAO entitiesDAO;

  @Autowired
  private AssetTypesDAO assetTypesDAO;

  @Autowired
  private ActionTypesDAO actionTypesDAO;

  @Autowired
  private RolesDAO rolesDAO;

  @Autowired
  private AttributeTypesDAO attributeTypesDAO;

  @Autowired
  private RelationTypesDAO relationTypesDAO;


  @Override
  @Transactional
  public PostRoleActionsResponse createRoleActions(
      PostRoleActionsRequest roleActionsRequest,
      User user
  ) throws
    RoleNotFoundException,
    EntityNotFoundException,
    AssetTypeNotFoundException,
    ActionTypeNotFoundException,
    RoleActionObjectNotFoundException
  {
    Role role = rolesDAO.findRoleById(UUID.fromString(roleActionsRequest.getRole_id()));

    PostRoleActionsResponse postRoleActionsResponse = new PostRoleActionsResponse(role.getRoleId());

    roleActionsRequest.getPrivilege_allowed().forEach(privilege -> createPrivilege(postRoleActionsResponse.getPrivilege_allowed(), privilege, PermissionType.ALLOW, role, user));
    roleActionsRequest.getPrivilege_not_allowed().forEach(privilege -> createPrivilege(postRoleActionsResponse.getPrivilege_allowed(), privilege, PermissionType.DENY, role, user));

    roleActionCachingService.evictByRoleId(role.getRoleId());

    return postRoleActionsResponse;
  }

  private void createPrivilege (List<PostPrivilegeResponse> list, PostPrivilegeRequest privilege, PermissionType permissionType, Role role, User user) {
    RoleAction roleAction = privilege.getAction_scope().equals(ActionScopeType.ONE_ID)
      ? createOneIdScopeRoleAction(privilege, permissionType, role, user)
      : createAllScopeRoleAction(privilege, permissionType, role, user);

    list.add(
      new PostPrivilegeResponse(
        roleAction.getRoleActionId(),
        roleAction.getActionType().getActionTypeId(),
        roleAction.getEntity().getId(),
        roleAction.getActionScopeType(),
        roleAction.getRoleActionObjectId(),
        roleAction.getOwnerUserOnly(),
        new Timestamp(System.currentTimeMillis()),
        roleAction.getCreatedByUUID()
      )
    );

    roleActionCachingService.evictByValueInKey(roleAction.getEntity().getEntityName().toString());
  }

  private RoleAction createOneIdScopeRoleAction (PostPrivilegeRequest privilege, PermissionType permissionType, Role role, User user) {
    if (privilege.getObject_type_id() == null) throw new RoleActionObjectNotFoundException();

    Entity entity = entitiesDAO.findEntityById(UUID.fromString(privilege.getEntity_type_id()));
    ActionType actionType = actionTypesDAO.findActionTypeById(UUID.fromString(privilege.getAction_type_id()));

    Role roleType = null;
    AssetType assetType = null;
    RelationType relationType = null;
    AttributeType attributeType = null;
    UUID objectId = UUID.fromString(privilege.getObject_type_id());

    switch (entity.getEntityName()) {
      case ASSET, ASSET_TYPE -> assetType = assetTypesDAO.findAssetTypeById(objectId);
      case RELATION, RELATION_TYPE -> relationType = relationTypesDAO.findRelationTypeById(objectId, false);
      case ATTRIBUTE, ATTRIBUTE_TYPE, RELATION_ATTRIBUTE, RELATION_COMPONENT_ATTRIBUTE -> attributeType = attributeTypesDAO.findAttributeTypeById(objectId, false);
      case ROLE, RESPONSIBILITY, RESPONSIBILITY_GLOBAL -> roleType = rolesDAO.findRoleById(objectId);
      default -> throw new RoleActionObjectNotFoundException();
    }

    return roleActionRepository.save(new RoleAction(
        role,
        actionType,
        entity,
        assetType,
        roleType,
        attributeType,
        relationType,
        privilege.getAction_scope(),
        permissionType,
        user
    ));
  }

  private RoleAction createAllScopeRoleAction (PostPrivilegeRequest privilege, PermissionType permissionType, Role role, User user) throws ActionTypeNotFoundException, EntityNotFoundException {
    Entity entity = entitiesDAO.findEntityById(UUID.fromString(privilege.getEntity_type_id()));

    ActionType actionType = actionTypesDAO.findActionTypeById(UUID.fromString(privilege.getAction_type_id()));

    if (privilege.getAction_scope().equals(ActionScopeType.ALL)) {
      // Clear old values because of potential change from ONE_ID to ALL
      deleteAllByRoleEntityAction(role.getRoleId(), entity.getId(), actionType.getActionTypeId(), null, user);
    }

    boolean ownerUserOnly = privilege.getOwner_user_only();
    if (!OWNER_USER_ID_ENTITIES.contains(entity.getEntityName())) {
      ownerUserOnly = false;
    }

    return roleActionRepository.save(new RoleAction(
      role,
      actionType,
      entity,
      privilege.getAction_scope(),
      permissionType,
      ownerUserOnly,
      user
    ));
  }

  @Override
  public GetRoleActionsResponse getRoleActionsByParams (
    UUID roleId,
    UUID entityTypeId,
    UUID actionTypeId,
    ActionScopeType actionScope,
    PermissionType permissionType,
    UUID objectTypeId,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<RoleAction> roleActions = roleActionRepository.findAllByParamsWithJoinedTablesPageable(
      roleId,
      entityTypeId,
      actionTypeId,
      actionScope,
      permissionType,
      objectTypeId,
      PageRequest.of(pageNumber, pageSize, Sort.by("roleActionId").ascending())
    );

    List<GetRoleActionResponse> roleActionResponse = roleActions.stream().map(roleAction -> new GetRoleActionResponse(
      roleAction.getRoleActionId(),
      roleAction.getRole().getRoleId(),
      roleAction.getRole().getRoleName(),
      roleAction.getEntity().getId(),
      roleAction.getEntity().getEntityName(),
      roleAction.getRoleActionObjectId(),
      roleAction.getRoleActionObjectName(),
      roleAction.getActionType().getActionTypeId(),
      roleAction.getActionType().getActionTypeName(),
      roleAction.getActionScopeType(),
      roleAction.getPermissionType(),
      roleAction.getOwnerUserOnly(),
      roleAction.getCreatedOn(),
      roleAction.getCreatedByUUID()
    )).toList();

    return new GetRoleActionsResponse(
      roleActions.getTotalElements(),
      pageSize,
      pageNumber,
      roleActionResponse
    );
  }

  @Override
  @Transactional
  public void deleteRoleActionById (UUID roleActionId, User user) {
    RoleAction roleAction = findRoleActionById(roleActionId);

    if (
      roleAction.getActionScopeType().equals(ActionScopeType.ALL) &&
      roleAction.getPermissionType().equals(PermissionType.ALLOW)
    ) {
      deleteAllByRoleEntityAction(roleAction.getRole().getRoleId(), roleAction.getEntity().getId(), roleAction.getActionType().getActionTypeId(), PermissionType.DENY, user);
    }

    roleAction.setIsDeleted(true);
    roleAction.setDeletedBy(user);
    roleAction.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    roleActionRepository.save(roleAction);

    roleActionCachingService.evictByRoleActionId(roleActionId);
  }

  private void deleteAllByRoleEntityAction (UUID roleId, UUID entityTypeId, UUID actionTypeId, PermissionType permissionType, User user) {
    List<RoleAction> roleActions = roleActionRepository.findAllByRoleEntityAction(roleId, entityTypeId, actionTypeId, permissionType);

    roleActions.forEach(roleAction -> {
      roleAction.setIsDeleted(true);
      roleAction.setDeletedBy(user);
      roleAction.setDeletedOn(new Timestamp(System.currentTimeMillis()));

      roleActionCachingService.evictByRoleActionId(roleAction.getRoleActionId());
    });

    roleActionRepository.saveAll(roleActions);
  }
}
