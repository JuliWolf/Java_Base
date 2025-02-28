package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.models;

import java.util.UUID;

/**
 * @author juliwolf
 */

public interface UserRoleResponsibilityCount {
  String getRoleIdText();

  default UUID getRoleId () {
    return UUID.fromString(getRoleIdText());
  }

  String getRoleName();

  Integer getRoleUsageCount();
}
