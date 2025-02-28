package ru.leroymerlin.datamanagement.spring.data_catalog_backend.models;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CacheResponse {
  private String key;
  private List<CacheRoleActionResponse> roleActionResponses;

  public void setRoleActionResponses (List<RoleActionResponse> roleActionResponses) {
    this.roleActionResponses = roleActionResponses.stream().map(response -> new CacheRoleActionResponse(
      response.getEntityName(),
      response.getPermissionType(),
      response.getActionScopeType(),
      response.getRoleActionId(),
      response.getRoleId(),
      response.getRoleTypeId(),
      response.getAssetTypeId(),
      response.getAttributeTypeId(),
      response.getRelationTypeId()
    )).toList();
  }

  class CacheRoleActionResponse {
    private EntityNameType entityNameType;

    private PermissionType permissionType;

    private ActionScopeType actionScopeType;

    private UUID roleActionId;

    private UUID roleId;

    private UUID roleTypeId;

    private UUID assetTypeId;

    private UUID attributeTypeId;

    private UUID relationTypeId;

    public EntityNameType getEntityNameType () {
      return entityNameType;
    }

    public PermissionType getPermissionType () {
      return permissionType;
    }

    public ActionScopeType getActionScopeType () {
      return actionScopeType;
    }

    public UUID getRoleActionId () {
      return roleActionId;
    }

    public UUID getRoleId () {
      return roleId;
    }

    public UUID getRoleTypeId () {
      return roleTypeId;
    }

    public UUID getAssetTypeId () {
      return assetTypeId;
    }

    public UUID getAttributeTypeId () {
      return attributeTypeId;
    }

    public UUID getRelationTypeId () {
      return relationTypeId;
    }

    public CacheRoleActionResponse (
      EntityNameType entityNameType,
      PermissionType permissionType,
      ActionScopeType actionScopeType,
      UUID roleActionId,
      UUID roleId,
      UUID roleTypeId,
      UUID assetTypeId,
      UUID attributeTypeId,
      UUID relationTypeId
    ) {
      this.entityNameType = entityNameType;
      this.permissionType = permissionType;
      this.actionScopeType = actionScopeType;
      this.roleActionId = roleActionId;
      this.roleId = roleId;
      this.roleTypeId = roleTypeId;
      this.assetTypeId = assetTypeId;
      this.attributeTypeId = attributeTypeId;
      this.relationTypeId = relationTypeId;
    }
  }
}
