package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "role_action", schema = "public")
public class RoleAction {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "role_action_id", updatable = false, nullable = false)
  private UUID roleActionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
  @ToString.Exclude
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_type_id", referencedColumnName = "action_type_id", nullable = false)
  @ToString.Exclude
  private ActionType actionType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "entity_type_id", referencedColumnName = "entity_id", nullable = false)
  @ToString.Exclude
  private Entity entity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  private AssetType assetType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_type_id", referencedColumnName = "role_id")
  @ToString.Exclude
  private Role roleType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_type_id", referencedColumnName = "attribute_type_id")
  @ToString.Exclude
  private AttributeType attributeType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_type_id", referencedColumnName = "relation_type_id")
  @ToString.Exclude
  private RelationType relationType;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_scope")
  private ActionScopeType actionScopeType;

  @Enumerated(EnumType.STRING)
  @Column(name = "permission_type", nullable = false)
  private PermissionType permissionType;

  @Column(name="owner_user_only")
  private Boolean ownerUserOnly = false;

  @Column(name = "created_on")
  @CreationTimestamp
  private java.sql.Timestamp createdOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User createdBy;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private java.sql.Timestamp deletedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User deletedBy;

  public UUID getRoleActionObjectId () {
    if (getActionScopeType().equals(ActionScopeType.ALL)) {
      return null;
    }

    return switch (getEntity().getEntityName()) {
      case ASSET, ASSET_TYPE -> getAssetType().getAssetTypeId();
      case RELATION, RELATION_TYPE -> getRelationType().getRelationTypeId();
      case ATTRIBUTE, ATTRIBUTE_TYPE -> getAttributeType().getAttributeTypeId();
      case ROLE, RESPONSIBILITY, RESPONSIBILITY_GLOBAL -> getRoleType().getRoleId();
      default -> null;
    };
  }

  public String getRoleActionObjectName () {
    if (getActionScopeType().equals(ActionScopeType.ALL)) {
      return null;
    }

    return switch (getEntity().getEntityName()) {
      case ROLE -> getRole().getRoleName();
      case ASSET_TYPE -> getAssetType().getAssetTypeName();
      case RELATION_TYPE -> getRelationType().getRelationTypeName();
      case ATTRIBUTE_TYPE -> getAttributeType().getAttributeTypeName();
      default -> null;
    };
  }

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public RoleAction(Role role, ActionType actionType, Entity entity, AssetType assetType, Role roleType, AttributeType attributeType, RelationType relationType, ActionScopeType actionScopeType, PermissionType permissionType, User createdBy) {
    this.role = role;
    this.actionType = actionType;
    this.entity = entity;
    this.assetType = assetType;
    this.roleType = roleType;
    this.attributeType = attributeType;
    this.relationType = relationType;
    this.actionScopeType = actionScopeType;
    this.permissionType = permissionType;
    this.createdBy = createdBy;
  }

  public RoleAction(Role role, ActionType actionType, Entity entity, ActionScopeType actionScopeType, PermissionType permissionType, User createdBy) {
    this.role = role;
    this.actionType = actionType;
    this.entity = entity;
    this.actionScopeType = actionScopeType;
    this.permissionType = permissionType;
    this.createdBy = createdBy;
  }

  public RoleAction(Role role, ActionType actionType, Entity entity, ActionScopeType actionScopeType, PermissionType permissionType, Boolean ownerUserOnly, User createdBy) {
    this.role = role;
    this.actionType = actionType;
    this.entity = entity;
    this.actionScopeType = actionScopeType;
    this.permissionType = permissionType;
    this.ownerUserOnly = ownerUserOnly;
    this.createdBy = createdBy;
  }
}
