package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;

/**
 * @author juliwolf
 */

public class RoleActionResponseImpl implements RoleActionResponse {
  private UUID roleActionId;
  private UUID roleId;
  private EntityNameType entityNameType;
  private PermissionType permissionType;
  private ActionScopeType actionScopeType;
  private UUID roleTypeId;
  private UUID assetTypeId;
  private UUID attributeTypeId;
  private UUID relationTypeId;

  private UUID assetId;

  public RoleActionResponseImpl (UUID roleActionId, EntityNameType entityNameType, PermissionType permissionType, ActionScopeType actionScopeType, UUID roleTypeId, UUID assetTypeId, UUID attributeTypeId, UUID relationTypeId) {
    this.roleActionId = roleActionId;
    this.entityNameType = entityNameType;
    this.permissionType = permissionType;
    this.actionScopeType = actionScopeType;
    this.roleTypeId = roleTypeId;
    this.assetTypeId = assetTypeId;
    this.attributeTypeId = attributeTypeId;
    this.relationTypeId = relationTypeId;
  }

  @Override
  public String getRoleActionIdText () {
    return null;
  }

  @Override
  public UUID getRoleActionId () {
    return roleActionId;
  }

  @Override
  public String getRoleIdText () {
    return null;
  }

  @Override
  public UUID getRoleId () {
    return roleId;
  }

  @Override
  public EntityNameType getEntityName () {
    return this.entityNameType;
  }

  @Override
  public PermissionType getPermissionType () {
    return this.permissionType;
  }

  @Override
  public ActionScopeType getActionScopeType () {
    return this.actionScopeType;
  }

  @Override
  public String getRoleTypeIdText () {
    return roleTypeId != null ? roleTypeId.toString() : null;
  }

  @Override
  public UUID getRoleTypeId () {
    return this.roleTypeId;
  }

  @Override
  public String getAssetTypeIdText () {
    return assetTypeId != null ? assetTypeId.toString() : null;
  }

  @Override
  public UUID getAssetTypeId () {
    return this.assetTypeId;
  }

  @Override
  public String getAttributeTypeIdText () {
    return attributeTypeId != null ? attributeTypeId.toString() : null;
  }

  @Override
  public UUID getAttributeTypeId () {
    return this.attributeTypeId;
  }

  @Override
  public String getRelationTypeIdText () {
    return relationTypeId != null ? relationTypeId.toString() : null;
  }

  @Override
  public UUID getRelationTypeId () {
    return this.relationTypeId;
  }

  @Override
  public String getAssetIdText () {
    return assetId != null ? assetId.toString() : null;
  }
}
