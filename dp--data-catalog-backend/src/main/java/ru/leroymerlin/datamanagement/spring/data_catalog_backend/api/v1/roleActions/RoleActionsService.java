package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes.ActionTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities.EntityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions.RoleActionObjectNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.get.GetRoleActionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

public interface RoleActionsService {
  PostRoleActionsResponse createRoleActions(
    PostRoleActionsRequest roleActionsRequest,
    User user
  ) throws
    RoleNotFoundException,
    EntityNotFoundException,
    AssetTypeNotFoundException,
    ActionTypeNotFoundException,
    RoleActionObjectNotFoundException;

  void deleteRoleActionById (UUID roleActionId, User user);

  GetRoleActionsResponse getRoleActionsByParams (
    UUID roleId,
    UUID entityTypeId,
    UUID actionTypeId,
    ActionScopeType actionScope,
    PermissionType permissionType,
    UUID objectTypeId,
    Integer pageNumber,
    Integer pageSize
  );
}
