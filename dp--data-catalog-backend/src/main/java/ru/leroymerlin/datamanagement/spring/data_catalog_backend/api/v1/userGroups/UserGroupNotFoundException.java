package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class UserGroupNotFoundException extends RuntimeException {
  public UserGroupNotFoundException (UUID userGroupId) {
    super("User group with id " + userGroupId + " not found");
  }

  public UserGroupNotFoundException () {
    super("user group not found");
  }
}
