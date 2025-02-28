package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.Getter;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.RoleActionEntityName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.UserRole;

/**
 * @author JuliWolf
 */
@Getter
@Setter
public class AuthUserDetails implements UserDetails {
  public static final String DEFAULT_CONNECTION_VALUES_KEY = "DEFAULT";

  private final User user;

  private List<GrantedAuthority> authorities;

  private Map<String, Map<RoleActionEntityName, String>> connectedValuesDict = new HashMap<>();

  private List<RoleActionResponse> allowedUserRoleAction = new ArrayList<>();
  private List<RoleActionResponse> deniedUserRoleAction = new ArrayList<>();

  private List<RoleActionResponse> allowedGroupRoleAction = new ArrayList<>();
  private List<RoleActionResponse> deniedGroupRoleAction = new ArrayList<>();

  private List<RoleActionResponse> allowedAssetUserRoleAction = new ArrayList<>();
  private List<RoleActionResponse> deniedAssetUserRoleAction = new ArrayList<>();

  private List<RoleActionResponse> allowedAssetGroupRoleAction = new ArrayList<>();
  private List<RoleActionResponse> deniedAssetGroupRoleAction = new ArrayList<>();

  private List<UserRole> userRoles = new ArrayList<>();
  private List<UserRole> groupUserRoles = new ArrayList<>();

  private Set<String> deniedIds = new HashSet<>();

  {
    connectedValuesDict.put(DEFAULT_CONNECTION_VALUES_KEY, new HashMap<>());
  }

  public void setUserRoleActions (List<RoleActionResponse> roleActions) {
    Map<PermissionType, List<RoleActionResponse>> collect = roleActions.stream().collect(Collectors.groupingBy(RoleActionResponse::getPermissionType));

    this.allowedUserRoleAction = collect.get(PermissionType.ALLOW) != null ? collect.get(PermissionType.ALLOW) : new ArrayList<>();
    this.deniedUserRoleAction = collect.get(PermissionType.DENY) != null ? collect.get(PermissionType.DENY) : new ArrayList<>();
  }

  public void setGroupRoleActions (List<RoleActionResponse> roleActions) {
    Map<PermissionType, List<RoleActionResponse>> collect = roleActions.stream().collect(Collectors.groupingBy(RoleActionResponse::getPermissionType));

    this.allowedGroupRoleAction = collect.get(PermissionType.ALLOW) != null ? collect.get(PermissionType.ALLOW) : new ArrayList<>();
    this.deniedGroupRoleAction = collect.get(PermissionType.DENY) != null ? collect.get(PermissionType.DENY) : new ArrayList<>();
  }

  public void setAssetUserRoleActions (List<RoleActionResponse> roleActions) {
    Map<PermissionType, List<RoleActionResponse>> collect = roleActions.stream().collect(Collectors.groupingBy(RoleActionResponse::getPermissionType));

    this.allowedAssetUserRoleAction = collect.get(PermissionType.ALLOW) != null ? collect.get(PermissionType.ALLOW) : new ArrayList<>();
    this.deniedAssetUserRoleAction = collect.get(PermissionType.DENY) != null ? collect.get(PermissionType.DENY) : new ArrayList<>();
  }

  public void setAssetGroupRoleActions (List<RoleActionResponse> roleActions) {
    Map<PermissionType, List<RoleActionResponse>> collect = roleActions.stream().collect(Collectors.groupingBy(RoleActionResponse::getPermissionType));

    this.allowedAssetGroupRoleAction = collect.get(PermissionType.ALLOW) != null ? collect.get(PermissionType.ALLOW) : new ArrayList<>();
    this.deniedAssetGroupRoleAction = collect.get(PermissionType.DENY) != null ? collect.get(PermissionType.DENY) : new ArrayList<>();
  }

  public AuthUserDetails(User user) {
    this.user = user;

    authorities = Stream.of("ROLE")
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public void setConnectedValue (RoleActionEntityName entityName, String value) {
    setConnectedValue(DEFAULT_CONNECTION_VALUES_KEY, entityName, value);
  }

  public void setConnectedValue (String key, RoleActionEntityName entityName, String value) {
    Map<RoleActionEntityName, String> entityNameMap = connectedValuesDict.computeIfAbsent(key, k -> new HashMap<>());

    entityNameMap.put(entityName, value);
  }

  public Map<RoleActionEntityName, String> getDefaultConnectionValues () {
    return connectedValuesDict.get(DEFAULT_CONNECTION_VALUES_KEY);
  }

  public String getConnectedValue (RoleActionEntityName entityName) {
    return getConnectedValue(DEFAULT_CONNECTION_VALUES_KEY, entityName);
  }

  public String getConnectedValue (String key, RoleActionEntityName entityName) {
    Map<RoleActionEntityName, String> actionEntityMap = getConnectedValueMap(key);

    return actionEntityMap.get(entityName);
  }

  public Map<RoleActionEntityName, String> getConnectedValueMap (String key) {
    Map<RoleActionEntityName, String> actionEntityMap = connectedValuesDict.get(key);

    if (actionEntityMap == null) {
      return new HashMap<>();
    }

    return actionEntityMap;
  }

  public void denyId (String key) {
    this.deniedIds.add(key);
  }

  public boolean hasDenied () {
    return !this.deniedIds.isEmpty();
  }
}
