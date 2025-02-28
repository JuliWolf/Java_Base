package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;

/**
 * @author juliwolf
 */

public interface RoleActionResponse {
  String getRoleActionIdText();
  default UUID getRoleActionId () {
    return UUID.fromString(getRoleActionIdText());
  }

  String getRoleIdText();
  default UUID getRoleId () {
    return getUUIDIfNotNull(getRoleIdText());
  }

  EntityNameType getEntityName();

  PermissionType getPermissionType();

  ActionScopeType getActionScopeType();

  String getRoleTypeIdText();
  default UUID getRoleTypeId () {
    return getUUIDIfNotNull(getRoleTypeIdText());
  }

  String getAssetTypeIdText();
  default UUID getAssetTypeId () {
    return getUUIDIfNotNull(getAssetTypeIdText());
  }

  String getAttributeTypeIdText();
  default UUID getAttributeTypeId () {
    return getUUIDIfNotNull(getAttributeTypeIdText());
  }

  String getRelationTypeIdText();
  default UUID getRelationTypeId () {
    return getUUIDIfNotNull(getRelationTypeIdText());
  }

  String getAssetIdText();
  default UUID getAssetId () {
    return UUID.fromString(getAssetIdText());
  }

  default UUID getUUIDIfNotNull (String value) {
    if (StringUtils.isEmpty(value)) return null;

    return UUID.fromString(value);
  }
}
