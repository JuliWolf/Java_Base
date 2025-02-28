package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.apache.commons.lang3.StringUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.BulkRoleActionByValue;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.BulkRoleActionItem;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.RoleActionCheckType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.RoleActionEntityName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.interfaces.AssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.interfaces.AttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.interfaces.RelationTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InsufficientPrivilegesException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.UserRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.BulkItemRequest;

import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails.DEFAULT_CONNECTION_VALUES_KEY;

/**
 * @author juliwolf
 */

public class RoleSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
  private final Authentication authentication;
  private final String EMPTY_VALUE = "empty";

  private Object filterObject;
  private Object returnObject;


  public void setPermissionEvaluator(PermissionEvaluator permissionEvaluator) {
    super.setPermissionEvaluator(permissionEvaluator);
  }

  public void setTrustResolver(AuthenticationTrustResolver trustResolver) {
    super.setTrustResolver(trustResolver);
  }

  public RoleSecurityExpressionRoot (MethodSecurityExpressionOperations expressionOperations) {
    super(expressionOperations.getAuthentication());

    this.authentication = expressionOperations.getAuthentication();
  }

  public Object getPrincipal() {
    return authentication.getPrincipal();
  }

  @Override
  public void setFilterObject (Object filterObject) {
    this.filterObject = filterObject;
  }

  @Override
  public Object getFilterObject () {
    return this.filterObject;
  }

  @Override
  public void setReturnObject (Object returnObject) {
    this.returnObject = returnObject;
  }

  @Override
  public Object getReturnObject () {
    return this.returnObject;
  }

  @Override
  public Object getThis () {
    return this;
  }

  @Override
  public boolean hasPermission (Object target, Object permission) {
    return super.hasPermission(target, permission);
  }

  @Override
  public boolean hasPermission (Object targetId, String targetType, Object permission) {
    return super.hasPermission(targetId, targetType, permission);
  }

  /*
   * Method to check if user have roles
   */
  public boolean hasAllowedRoles (String... allowedRoles) {
    List<String> roleList = Arrays.stream(allowedRoles).toList();
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    Optional<UserRole> containsUserRole = userDetails.getUserRoles().stream().filter(role -> roleList.contains(role.getRoleName())).findFirst();

    if (containsUserRole.isPresent()) return true;

    Optional<UserRole> containsGroupRole = userDetails.getGroupUserRoles().stream().filter(role -> roleList.contains(role.getRoleName())).findFirst();

    if (containsGroupRole.isPresent()) return true;

    throw new InsufficientPrivilegesException();
  }

  /*
  * Method to check if user have global responsibilities or/and asset
  */
  public boolean isMethodAllowed () {
    return isMethodAllowed(true);
  }

  public boolean isMethodAllowed (boolean isThrow) throws InsufficientPrivilegesException {
    try {
      return checkIsGeneralMethodAllowed(
        DEFAULT_CONNECTION_VALUES_KEY,
        getDefaultBulkItemRoleAction()
      );
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isAssetTypeAllowed (String assetTypeId) {
    return isAssetTypeAllowed(assetTypeId, true);
  }

  public boolean isAssetTypeAllowed (String assetTypeId, boolean isThrow) {
    try {
      RoleActionCheckType roleActionCheckType = isMethodAllowedByValue(assetTypeId, RoleActionEntityName.ASSET_TYPE);

      return makeDecisionOnRoleActionCheckType(roleActionCheckType, assetTypeId);
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isRoleAllowed (String roleId) {
    return isRoleAllowed(roleId, true);
  }

  public boolean isRoleAllowed (String roleId, boolean isThrow) {
    try {
      RoleActionCheckType roleActionCheckType = isMethodAllowedByValue(roleId, RoleActionEntityName.ROLE);

      return makeDecisionOnRoleActionCheckType(roleActionCheckType, roleId);
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isAttributeTypeAllowed (String attributeTypeId) {
    return isAttributeTypeAllowed(attributeTypeId, true);
  }

  public boolean isAttributeTypeAllowed (String attributeTypeId, boolean isThrow) {
    try {
      RoleActionCheckType roleActionCheckType = isMethodAllowedByValue(attributeTypeId, RoleActionEntityName.ATTRIBUTE_TYPE);

      return makeDecisionOnRoleActionCheckType(roleActionCheckType, attributeTypeId);
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isRelationTypeAllowed (String relationTypeId) {
    return isRelationTypeAllowed(relationTypeId, true);
  }

  public boolean isRelationTypeAllowed (String relationTypeId, boolean isThrow) {
    try {
      RoleActionCheckType roleActionCheckType = isMethodAllowedByValue(relationTypeId, RoleActionEntityName.RELATION_TYPE);

      return makeDecisionOnRoleActionCheckType(roleActionCheckType, relationTypeId);
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isAssetAllowed () {
    return isAssetAllowed(false);
  }

  public boolean isAssetAllowed (boolean anyAllowed) {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    BulkRoleActionItem bulkItemRoleAction = getDefaultBulkItemRoleAction(true);

    if (!bulkItemRoleAction.hasAllowed()) {
      throw new InsufficientPrivilegesException();
    }

    return checkIsAssetAllowed(
      userDetails.getDefaultConnectionValues(),
      bulkItemRoleAction,
      anyAllowed
    );
  }
  
  public boolean isAssetRelationTypeAllowed (String relationTypeId) {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    return checkIsAssetRelationTypeAllowed(
      relationTypeId,
      userDetails.getDefaultConnectionValues(),
      getDefaultBulkItemRoleAction(true)
    );
  }

  public boolean isOwner (String userId) {
    return isOwnerEqualsUser(userId);
  }

  public boolean isOwner () {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    String userId = userDetails.getConnectedValue(RoleActionEntityName.OWNER_ID);

    return isOwnerEqualsUser(userId);
  }

  /*
  * Bulk
  */
  public boolean isUpdateBulkMethodAllowed (List<BulkItemRequest> itemRequests) {
    return isUpdateBulkMethodAllowed(itemRequests, true);
  }

  public boolean isUpdateBulkMethodAllowed (List<BulkItemRequest> itemRequests, boolean isThrow) {
    List<UUID> list = itemRequests.stream().map(BulkItemRequest::getRequestItemId).filter(Objects::nonNull).toList();
    return isBulkMethodAllowed(list, isThrow);
  }

  public boolean isBulkMethodAllowed (List<UUID> requestIds) {
    return isBulkMethodAllowed(requestIds, true);
  }

  public boolean isBulkMethodAllowed (List<UUID> requestIds, boolean isThrow) throws InsufficientPrivilegesException {
    try {
      BulkRoleActionByValue bulkRoleAction = mapBulkRoleActions(RoleActionEntityName.DEFAULT, false);

      requestIds.forEach(uuid -> {
        BulkRoleActionItem bulkRoleActionItem = mapBulkItemRoleActions(null, bulkRoleAction);

        checkIsGeneralMethodAllowed(
          uuid.toString(),
          bulkRoleActionItem,
          !isThrow
        );
      });

      if (hasDenied()) {
        throw new InsufficientPrivilegesException();
      }

      return true;
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isAssetTypesInBulkAllowed (List<AssetTypeRequest> requestsWithAssetType) {
    return isAssetTypesInBulkAllowed(requestsWithAssetType, true);
  }

  public boolean isAssetTypesInBulkAllowed (List<AssetTypeRequest> requestsWithAssetType, boolean isThrow) {
    try {
      requestsWithAssetType.forEach(request ->
        checkBulkRequestItemByRoleActionEntityName(
          request.getAsset_type_id(),
          RoleActionEntityName.ASSET_TYPE,
          mapBulkRoleActions(RoleActionEntityName.ASSET_TYPE, false),
          !isThrow
        )
      );

      if (hasDenied()) {
        throw new InsufficientPrivilegesException();
      }

      return true;
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isAttributeTypesInBulkAllowed (List<AttributeTypeRequest> requestsWithAttributeType) {
    return isAttributeTypesInBulkAllowed(requestsWithAttributeType, true);
  }

  public boolean isAttributeTypesInBulkAllowed (List<AttributeTypeRequest> requestsWithAttributeType, boolean isThrow) {
    try {
      requestsWithAttributeType.forEach(request ->
        checkBulkRequestItemByRoleActionEntityName(
          request.getAttribute_type_id(),
          RoleActionEntityName.ATTRIBUTE_TYPE,
          mapBulkRoleActions(RoleActionEntityName.ATTRIBUTE_TYPE, false),
          !isThrow
        )
      );

      if (hasDenied()) {
        throw new InsufficientPrivilegesException();
      }

      return true;
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isRolesInBulkAllowed (List<BulkItemRequest> requestsWithRole) {
    return isRolesInBulkAllowed(requestsWithRole, true);
  }

  public boolean isRolesInBulkAllowed (List<BulkItemRequest> requestsWithRole, boolean isThrow) {
    try {
      requestsWithRole.forEach(request ->
        checkBulkRequestItemByRoleActionEntityName(
          request.getRequestItemId().toString(),
          RoleActionEntityName.ROLE,
          mapBulkRoleActions(RoleActionEntityName.ROLE, false),
          !isThrow
        )
      );

      if (hasDenied()) {
        throw new InsufficientPrivilegesException();
      }

      return true;
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isRelationTypesInBulkAllowed (List<RelationTypeRequest> requestsWithRelationType) {
    return isRelationTypesInBulkAllowed(requestsWithRelationType, true);
  }

  public boolean isRelationTypesInBulkAllowed (List<RelationTypeRequest> requestsWithRelationType, boolean isThrow) {
    try {
      requestsWithRelationType.forEach(request ->
        checkBulkRequestItemByRoleActionEntityName(
          request.getRelation_type_id(),
          RoleActionEntityName.RELATION_TYPE,
          mapBulkRoleActions(RoleActionEntityName.RELATION_TYPE, false),
          !isThrow
        )
      );

      if (hasDenied()) {
        throw new InsufficientPrivilegesException();
      }

      return true;
    } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
      if (isThrow) {
        throw insufficientPrivilegesException;
      }

      return false;
    }
  }

  public boolean isAssetsInBulkAllowed (List<BulkItemRequest> requestsWithAsset) {
    List<UUID> ids = requestsWithAsset.stream()
      .map(BulkItemRequest::getRequestItemId)
      .filter(Objects::nonNull)
      .toList();

    return isAssetsIdsInBulkAllowed(ids, false);
  }

  public boolean isAssetsIdsInBulkAllowed (List<UUID> assetsRequest) {
    return isAssetsIdsInBulkAllowed(assetsRequest, false);
  }

  public boolean isAssetsIdsInBulkAllowed (List<UUID> assetsRequest, boolean anyAllowed) {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
    BulkRoleActionItem bulkRoleActionItem = getDefaultBulkItemRoleAction(true);

    assetsRequest
      .forEach(request -> {
        checkIsAssetAllowed(
          userDetails.getConnectedValuesDict().get(request.toString()),
          bulkRoleActionItem,
          anyAllowed
        );
      });

    if (
      bulkRoleActionItem.getAllowedUserRoleActions().isEmpty() &&
      bulkRoleActionItem.getAllowedGroupRoleActions().isEmpty()
    ) {
      throw new InsufficientPrivilegesException();
    }

    return true;
  }

  public boolean isBulkAssetRelationTypeAllowed (List<RelationTypeRequest> requestsWithRelationType) {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
    BulkRoleActionByValue bulkRoleAction = mapBulkRoleActions(RoleActionEntityName.RELATION_TYPE, true);

    requestsWithRelationType.stream()
      .filter(item -> userDetails.getDeniedIds().isEmpty() || userDetails.getDeniedIds().contains(item.getRelation_type_id()))
      .forEach(item -> {
        BulkRoleActionItem bulkItem = mapBulkItemRoleActions(item.getRelation_type_id(), bulkRoleAction);

        checkIsAssetRelationTypeAllowed(
          item.getRelation_type_id(),
          userDetails.getConnectedValueMap(item.getRelation_type_id()),
          bulkItem
        );
    });

    return true;
  }

  private boolean checkIsAssetRelationTypeAllowed (
    String relationTypeId,
    Map<RoleActionEntityName, String> connectedValueMap,
    BulkRoleActionItem bulkRoleActionItem
  ) {
    RoleActionCheckType roleActionCheckType = isMethodAllowedByValue(
      relationTypeId,
      RoleActionEntityName.RELATION_TYPE,
      bulkRoleActionItem
    );

    makeDecisionOnRoleActionCheckType(roleActionCheckType);

    return checkIsAssetAllowed(
      connectedValueMap,
      bulkRoleActionItem,
      true
    );
  }

  private void checkBulkRequestItemByRoleActionEntityName(
    String itemValue,
    RoleActionEntityName roleActionEntityName,
    BulkRoleActionByValue bulkRoleAction,
    Boolean checkAll
  ) throws InsufficientPrivilegesException {
    BulkRoleActionItem bulkRoleActionItem = mapBulkItemRoleActions(itemValue, bulkRoleAction);

    RoleActionCheckType isAllowed = isMethodAllowedByValue(
      itemValue,
      roleActionEntityName,
      bulkRoleActionItem
    );

    checkRoleActionCheckTypeAndMakeDecision(isAllowed, itemValue, checkAll);
  }

  private boolean checkIsGeneralMethodAllowed (
    String connectedValueKey,
    BulkRoleActionItem bulkRoleActionItem
  ) throws InsufficientPrivilegesException {
    return checkIsGeneralMethodAllowed(
      connectedValueKey,
      bulkRoleActionItem,
      false
    );
  }

  private boolean checkIsGeneralMethodAllowed (
    String connectedValueKey,
    BulkRoleActionItem bulkRoleActionItem,
    Boolean checkAll
  ) throws InsufficientPrivilegesException {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    boolean hasAllowedUserRoleActions = bulkRoleActionItem.getAllowedUserRoleActions() != null && !bulkRoleActionItem.getAllowedUserRoleActions().isEmpty();
    boolean hasAllowedGroupRoleActions = bulkRoleActionItem.getAllowedGroupRoleActions() != null && !bulkRoleActionItem.getAllowedGroupRoleActions().isEmpty();

    if (!hasAllowedUserRoleActions && !hasAllowedGroupRoleActions) {
      throw new InsufficientPrivilegesException();
    }

    if (!userDetails.getConnectedValueMap(connectedValueKey).isEmpty()) {
      userDetails.getConnectedValueMap(connectedValueKey)
        .forEach((key, value) -> {
          RoleActionCheckType roleActionCheckType = isMethodAllowedByValue(
            value,
            key,
            bulkRoleActionItem
          );

          checkRoleActionCheckTypeAndMakeDecision(roleActionCheckType, value, checkAll);
        });
    }

    if (hasDenied()) {
      if (checkAll) {
        return false;
      }

      throw new InsufficientPrivilegesException();
    }

    return true;
  }

  private boolean isOwnerEqualsUser (String userId) {
    if (StringUtils.isEmpty(userId)) {
      throw new InsufficientPrivilegesException();
    }

    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    if (!userId.equals(userDetails.getUser().getUserId().toString())) {
      throw new InsufficientPrivilegesException();
    }

    return true;
  }

  private RoleActionCheckType isMethodAllowedByValue (String value, RoleActionEntityName connectedValueEntityName) {
    return isMethodAllowedByValue(
      value,
      connectedValueEntityName,
      getDefaultBulkItemRoleAction()
    );
  }

  private boolean checkIsAssetAllowed (
    Map<RoleActionEntityName, String> connectedValueMap,
    BulkRoleActionItem bulkRoleActionItem,
    boolean anyAllowed
  ) {
    if (connectedValueMap != null && !connectedValueMap.isEmpty()) {
      AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

      connectedValueMap.entrySet().stream()
        .filter(entry -> userDetails.getDeniedIds().isEmpty() || userDetails.getDeniedIds().contains(entry.getValue()))
        .forEach(entry -> {
          RoleActionCheckType roleActionCheckType = isMethodAllowedByValue(
            entry.getValue(),
            entry.getKey(),
            bulkRoleActionItem
          );

          makeDecisionOnRoleActionCheckType(roleActionCheckType, entry.getValue());
        }
      );
    }

    return makeDecisionOnRoleActionCheckType(
      isAllowedByAssetRoleActions(
        bulkRoleActionItem,
        anyAllowed
      ),
      null,
      !anyAllowed
    );
  }

  private RoleActionCheckType isMethodAllowedByValue (
    String value,
    RoleActionEntityName connectedValueEntityName,
    BulkRoleActionItem bulkRoleActionItem
  ) {
    RoleActionCheckType roleActionCheckType = getRoleActionCheckTypeByValue(
      bulkRoleActionItem.getAllowedUserRoleActions(),
      bulkRoleActionItem.getDeniedUserRoleActions(),
      value,
      connectedValueEntityName
    );

    if (
      roleActionCheckType.equals(RoleActionCheckType.ALLOW_ALL) ||
      roleActionCheckType.equals(RoleActionCheckType.ALLOW_ONE_ID)
    ) return roleActionCheckType;

    // Group check
   return getRoleActionCheckTypeByValue(
      bulkRoleActionItem.getAllowedGroupRoleActions(),
      bulkRoleActionItem.getDeniedGroupRoleActions(),
      value,
      connectedValueEntityName
    );
  }

  private RoleActionCheckType getRoleActionCheckTypeByValue (
    List<RoleActionResponse> allowedRoleActions,
    List<RoleActionResponse> deniedRoleActions,
    String value,
    RoleActionEntityName connectedValueEntityName
  ) {
    Optional<RoleActionResponse> deniedAction = deniedRoleActions.stream()
      .filter(roleAction -> isValueAllowedByEntityType(roleAction, value, connectedValueEntityName))
      .findFirst();

    if (deniedAction.isPresent()) {
      return RoleActionCheckType.DENY_ONE_ID;
    }

    for (RoleActionResponse allowedRoleAction : allowedRoleActions) {
      if (allowedRoleAction.getActionScopeType().equals(ActionScopeType.ALL)) {
        return RoleActionCheckType.ALLOW_ALL;
      }

      boolean isAllowed = isValueAllowedByEntityType(allowedRoleAction, value, connectedValueEntityName);

      // ROLE vs ROLE -> true
      // ROLE vs RESPONSIBILITY -> false
      if (
        isAllowed &&
        allowedRoleAction.getEntityName().isEntityAllowed(EntityNameType.valueOf(connectedValueEntityName.toString()))
      ) {
        return RoleActionCheckType.ALLOW_ONE_ID;
      }
    }

    return RoleActionCheckType.DENY_ALL;
  }
  
  private RoleActionCheckType isAllowedByAssetRoleActions (
    BulkRoleActionItem bulkRoleActionItem,
    boolean anyAllowed
  ) {
    RoleActionCheckType isAllowed = getRoleActionCheckTypeByAssetRoleActions(
      bulkRoleActionItem.getAllowedUserRoleActions(),
      bulkRoleActionItem.getDeniedUserRoleActions(),
      anyAllowed
    );

    if (isAllowed.equals(RoleActionCheckType.ALLOW_ALL)) {
      return isAllowed;
    }

    // Group check
    return getRoleActionCheckTypeByAssetRoleActions(
      bulkRoleActionItem.getAllowedGroupRoleActions(),
      bulkRoleActionItem.getDeniedGroupRoleActions(),
      anyAllowed
    );
  }

  private RoleActionCheckType getRoleActionCheckTypeByAssetRoleActions (
    List<RoleActionResponse> allowedRoleActions,
    List<RoleActionResponse> deniedRoleActions,
    boolean anyAllowed
  ) {
    if (!anyAllowed && !deniedRoleActions.isEmpty()) {
      return RoleActionCheckType.DENY_ALL;
    }

    if (allowedRoleActions.isEmpty()) {
      return RoleActionCheckType.DENY_ALL;
    }

    return RoleActionCheckType.ALLOW_ALL;
  }

  private void checkRoleActionCheckTypeAndMakeDecision (
    RoleActionCheckType roleActionCheckType,
    String value,
    boolean checkAll
  ) {
    if (checkAll) {
      makeDecisionOnRoleActionCheckType(roleActionCheckType, value, false);

      return;
    }

    makeDecisionOnRoleActionCheckType(roleActionCheckType, value);
  }

  private boolean makeDecisionOnRoleActionCheckType (RoleActionCheckType roleActionCheckType) throws InsufficientPrivilegesException {
    return makeDecisionOnRoleActionCheckType(roleActionCheckType, null, true);
  }

  private boolean makeDecisionOnRoleActionCheckType (RoleActionCheckType roleActionCheckType, String value) throws InsufficientPrivilegesException {
    return makeDecisionOnRoleActionCheckType(roleActionCheckType, value, true);
  }

  private boolean makeDecisionOnRoleActionCheckType (RoleActionCheckType roleActionCheckType, String value, Boolean isThrow) throws InsufficientPrivilegesException {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    if (
      roleActionCheckType.equals(RoleActionCheckType.ALLOW_ALL) ||
      roleActionCheckType.equals(RoleActionCheckType.ALLOW_ONE_ID)
    ) return true;

    if (value != null) {
      userDetails.denyId(value);
    }

    if (isThrow) {
      throw new InsufficientPrivilegesException();
    }

    return false;
  }

  private BulkRoleActionByValue mapBulkRoleActions (RoleActionEntityName entityName, boolean isAsset) {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    // User role actions
    List<RoleActionResponse> allowedUserRoleAction = isAsset ? userDetails.getAllowedAssetUserRoleAction() : userDetails.getAllowedUserRoleAction();
    List<RoleActionResponse> deniedUserRoleAction = isAsset ? userDetails.getDeniedAssetUserRoleAction() : userDetails.getDeniedUserRoleAction();

    Map<String, List<RoleActionResponse>> allowedRoleActions = allowedUserRoleAction.stream()
      .collect(Collectors.groupingBy(item -> getValueByEntityType(item, entityName)));
    Map<String, List<RoleActionResponse>> deniedRoleActions = deniedUserRoleAction.stream()
      .collect(Collectors.groupingBy(item -> getValueByEntityType(item, entityName)));

    // Group role actions
    List<RoleActionResponse> allowedGroupUserRoleAction = isAsset ? userDetails.getAllowedAssetGroupRoleAction() : userDetails.getAllowedGroupRoleAction();
    List<RoleActionResponse> deniedGroupUserRoleAction = isAsset ? userDetails.getDeniedAssetGroupRoleAction() : userDetails.getDeniedGroupRoleAction();

    Map<String, List<RoleActionResponse>> allowedGroupRoleActions = allowedGroupUserRoleAction.stream()
      .collect(Collectors.groupingBy(item -> getValueByEntityType(item, entityName)));
    Map<String, List<RoleActionResponse>> deniedGroupRoleActions = deniedGroupUserRoleAction.stream()
      .collect(Collectors.groupingBy(item -> getValueByEntityType(item, entityName)));

    return new BulkRoleActionByValue(
      allowedRoleActions,
      deniedRoleActions,
      allowedGroupRoleActions,
      deniedGroupRoleActions
    );
  }

  private BulkRoleActionItem mapBulkItemRoleActions (String requestId, BulkRoleActionByValue bulkRoleAction) {
    // User role actions
    List<RoleActionResponse> allowedRoleActions = getRoleActionsList(bulkRoleAction.getAllowedUserRoleActions().get(EMPTY_VALUE));
    allowedRoleActions.addAll(getRoleActionsList(bulkRoleAction.getAllowedUserRoleActions().get(requestId)));

    List<RoleActionResponse> deniedRoleActions = getRoleActionsList(bulkRoleAction.getDeniedUserRoleActions().get(EMPTY_VALUE));
    deniedRoleActions.addAll(getRoleActionsList(bulkRoleAction.getDeniedUserRoleActions().get(requestId)));

    // Group role actions
    List<RoleActionResponse> allowedGroupRoleActions = getRoleActionsList(bulkRoleAction.getAllowedGroupRoleActions().get(EMPTY_VALUE));
    allowedGroupRoleActions.addAll(getRoleActionsList(bulkRoleAction.getAllowedGroupRoleActions().get(requestId)));

    List<RoleActionResponse> deniedGroupRoleActions = getRoleActionsList(bulkRoleAction.getDeniedGroupRoleActions().get(EMPTY_VALUE));
    deniedGroupRoleActions.addAll(getRoleActionsList(bulkRoleAction.getDeniedGroupRoleActions().get(requestId)));

    return new BulkRoleActionItem(
      allowedRoleActions,
      deniedRoleActions,
      allowedGroupRoleActions,
      deniedGroupRoleActions
    );
  }

  private BulkRoleActionItem getDefaultBulkItemRoleAction () {
    return getDefaultBulkItemRoleAction(false);
  }

  private BulkRoleActionItem getDefaultBulkItemRoleAction (boolean isAsset) {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    List<RoleActionResponse> allowedUserRoleAction = isAsset ? userDetails.getAllowedAssetUserRoleAction() : userDetails.getAllowedUserRoleAction();
    List<RoleActionResponse> deniedUserRoleAction = isAsset ? userDetails.getDeniedAssetUserRoleAction() : userDetails.getDeniedUserRoleAction();

    List<RoleActionResponse> allowedGroupRoleActions = isAsset ? userDetails.getAllowedAssetGroupRoleAction() : userDetails.getAllowedGroupRoleAction();
    List<RoleActionResponse> deniedGroupRoleActions = isAsset ? userDetails.getDeniedAssetGroupRoleAction() : userDetails.getDeniedGroupRoleAction();

    return new BulkRoleActionItem(
      allowedUserRoleAction,
      deniedUserRoleAction,
      allowedGroupRoleActions,
      deniedGroupRoleActions
    );
  }

  private String getValueByEntityType (RoleActionResponse item, RoleActionEntityName entityName) {
    return switch (entityName) {
      case ROLE -> item.getRoleId() != null ? item.getRoleId().toString() : EMPTY_VALUE;
      case ASSET_ID -> item.getAssetId() != null ? item.getAssetId().toString() : EMPTY_VALUE;
      case ASSET_TYPE -> item.getAssetTypeId() != null ? item.getAssetTypeId().toString() : EMPTY_VALUE;
      case RELATION_TYPE -> item.getRelationTypeId() != null ? item.getRelationTypeId().toString() : EMPTY_VALUE;
      case ATTRIBUTE_TYPE -> item.getAttributeTypeId() != null ? item.getAttributeTypeId().toString() : EMPTY_VALUE;
      case OWNER_ID, DEFAULT -> EMPTY_VALUE;
    };
  }

  private boolean isValueAllowedByEntityType (RoleActionResponse allowedRoleAction, String value, RoleActionEntityName entityName) {
    return switch (entityName) {
      case ROLE -> allowedRoleAction.getRoleTypeIdText() != null && allowedRoleAction.getRoleTypeId().toString().equals(value);
      case ASSET_TYPE -> allowedRoleAction.getAssetTypeIdText() != null && allowedRoleAction.getAssetTypeId().toString().equals(value);
      case RELATION_TYPE -> allowedRoleAction.getRelationTypeIdText() != null && allowedRoleAction.getRelationTypeId().toString().equals(value);
      case ATTRIBUTE_TYPE -> allowedRoleAction.getAttributeTypeIdText() != null && allowedRoleAction.getAttributeTypeId().toString().equals(value);
      case DEFAULT, ASSET_ID, OWNER_ID -> false;
    };
  }

  private <T> List<T> getRoleActionsList (List<T> roleActions) {
    return Optional.ofNullable(roleActions).orElse(new ArrayList<>());
  }

  private boolean hasDenied () {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    return userDetails.hasDenied();
  }
}
