package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.RoleActionEntityName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.UserRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;

/**
 * @author JuliWolf
 */
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    if (!customUser.isAuthorized()) {
      return context;
    }

    User user = new User(
      customUser.username(),
      customUser.firstName(),
      "lastName",
      SourceType.KEYCLOAK,
      "test@mail.ru"
    );

    user.setUserId(new UUID(5, 5));

    AuthUserDetails principal = new AuthUserDetails(
      user
    );

    setRoleActions(customUser, principal);
    setRoles(customUser, principal);

    Map<RoleActionEntityName, String> connectedValuesDict = principal.getDefaultConnectionValues();
    connectedValuesDict.put(RoleActionEntityName.OWNER_ID, user.getUserId().toString());

    Authentication auth = UsernamePasswordAuthenticationToken.authenticated(principal, "password", principal.getAuthorities());

    context.setAuthentication(auth);
    return context;
  }

  private void setRoleActions (WithMockCustomUser customUser, AuthUserDetails principal) {
    List<RoleActionResponse> allowedUserRoleActions = new ArrayList<>();
    List<RoleActionResponse> allowedGroupRoleActions = new ArrayList<>();

    List<RoleActionResponse> allowedUserAssetRoleActions = new ArrayList<>();

    if (customUser.hasUserAllAllowedRoleActions()) {
      allowedUserRoleActions.addAll(createAllActionScopeRoleActions(PermissionType.ALLOW));
    }

    if (customUser.hasUserAssetAllowedRoleActions()) {
      allowedUserAssetRoleActions.addAll(createAllActionScopeRoleActions(PermissionType.ALLOW));
    }

    if (customUser.hasUserAllowedRoleId()) {
      allowedUserRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ROLE, PermissionType.ALLOW, ActionScopeType.ONE_ID, new UUID(123, 123), null, null, null));
    }

    if (customUser.hasUserAllowedAssetTypeId()) {
      allowedUserRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ASSET_TYPE, PermissionType.ALLOW, ActionScopeType.ONE_ID, null, new UUID(123, 123), null, null));
    }

    if (customUser.hasUserAllowedAttributeTypeId()) {
      allowedUserRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ATTRIBUTE_TYPE, PermissionType.ALLOW, ActionScopeType.ONE_ID, null, null, new UUID(123, 123), null));
    }

    if (customUser.hasUserAllowedRelationTypeId()) {
      allowedUserRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.RELATION_TYPE, PermissionType.ALLOW, ActionScopeType.ONE_ID, null, null, null, new UUID(123, 123)));
    }

    // Group
    if (customUser.hasGroupAllAllowedRoleActions()) {
      allowedGroupRoleActions.addAll(createAllActionScopeRoleActions(PermissionType.ALLOW));
    }

    if (customUser.hasGroupAllowedRoleId()) {
      allowedGroupRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ROLE, PermissionType.ALLOW, ActionScopeType.ONE_ID, new UUID(123, 123), null, null, null));
    }

    if (customUser.hasGroupAllowedAssetTypeId()) {
      allowedGroupRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ASSET_TYPE, PermissionType.ALLOW, ActionScopeType.ONE_ID, null, new UUID(123, 123), null, null));
    }

    if (customUser.hasGroupAllowedAttributeTypeId()) {
      allowedGroupRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ATTRIBUTE_TYPE, PermissionType.ALLOW, ActionScopeType.ONE_ID, null, null, new UUID(123, 123), null));
    }

    if (customUser.hasGroupAllowedRelationTypeId()) {
      allowedGroupRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.RELATION_TYPE, PermissionType.ALLOW, ActionScopeType.ONE_ID, null, null, null, new UUID(123, 123)));
    }

    principal.setAssetUserRoleActions(allowedUserAssetRoleActions);
    principal.setGroupRoleActions(allowedGroupRoleActions);
    principal.setAllowedUserRoleAction(allowedUserRoleActions);

    List<RoleActionResponse> deniedUserRoleActions = new ArrayList<>();
    List<RoleActionResponse> deniedGroupRoleActions = new ArrayList<>();
    List<RoleActionResponse> deniedAssetRoleActions = new ArrayList<>();

    if (customUser.hasUserAllDeniedRoleActions()) {
      deniedUserRoleActions.addAll(createAllActionScopeRoleActions(PermissionType.DENY));
    }

    if (customUser.hasUserAssetDeniedRoleActions()) {
      deniedAssetRoleActions.addAll(createAllActionScopeRoleActions(PermissionType.DENY));
    }

    if (customUser.hasUserDeniedRoleId()) {
      deniedUserRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ROLE, PermissionType.DENY, ActionScopeType.ONE_ID, new UUID(123, 123), null, null, null));
    }

    if (customUser.hasUserDeniedAssetTypeId()) {
      deniedUserRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ASSET_TYPE, PermissionType.DENY, ActionScopeType.ONE_ID, null, new UUID(123, 123), null, null));
    }

    if (customUser.hasUserDeniedAttributeTypeId()) {
      deniedUserRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ATTRIBUTE_TYPE, PermissionType.DENY, ActionScopeType.ONE_ID, null, null, new UUID(123, 123), null));
    }

    if (customUser.hasUserDeniedRelationTypeId()) {
      deniedUserRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.RELATION_TYPE, PermissionType.DENY, ActionScopeType.ONE_ID, null, null, null, new UUID(123, 123)));
    }

    // Group
    if (customUser.hasGroupAllDeniedRoleActions()) {
      deniedGroupRoleActions.addAll(createAllActionScopeRoleActions(PermissionType.DENY));
    }

    if (customUser.hasGroupDeniedRoleId()) {
      deniedGroupRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ROLE, PermissionType.DENY, ActionScopeType.ONE_ID, new UUID(123, 123), null, null, null));
    }

    if (customUser.hasGroupDeniedAssetTypeId()) {
      deniedGroupRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ASSET_TYPE, PermissionType.DENY, ActionScopeType.ONE_ID, null, new UUID(123, 123), null, null));
    }

    if (customUser.hasGroupDeniedAttributeTypeId()) {
      deniedGroupRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.ATTRIBUTE_TYPE, PermissionType.DENY, ActionScopeType.ONE_ID, null, null, new UUID(123, 123), null));
    }

    if (customUser.hasGroupDeniedRelationTypeId()) {
      deniedGroupRoleActions.add(new RoleActionResponseImpl(UUID.randomUUID(), EntityNameType.RELATION_TYPE, PermissionType.DENY, ActionScopeType.ONE_ID, null, null, null, new UUID(123, 123)));
    }

    principal.setDeniedUserRoleAction(deniedUserRoleActions);
    principal.setDeniedGroupRoleAction(deniedGroupRoleActions);
    principal.setDeniedAssetUserRoleAction(deniedAssetRoleActions);
  }

  private void setRoles (WithMockCustomUser customUser, AuthUserDetails principal) {
    List<UserRole> userRoles = new ArrayList<>();
    List<UserRole> groupUserRoles = new ArrayList<>();

    for (int i = 0; i < customUser.allowedRoles().length; i++) {
      UserRole role = new UserRole(UUID.randomUUID(), customUser.allowedRoles()[i]);
      userRoles.add(role);
      groupUserRoles.add(role);
    }

    principal.setUserRoles(userRoles);
    principal.setGroupUserRoles(groupUserRoles);
  }

  private List<RoleActionResponse> createAllActionScopeRoleActions (PermissionType permissionType) {
    List<RoleActionResponse> roleActions = new ArrayList<>();
    for (EntityNameType type : EntityNameType.values()) {
      roleActions.add(new RoleActionResponseImpl(
        UUID.randomUUID(),
        type,
        permissionType,
        ActionScopeType.ALL,
        null, null, null, null
      ));
    }

    return roleActions;
  }
}
