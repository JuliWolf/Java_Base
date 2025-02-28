package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class RoleActionNotFoundException extends RuntimeException {
  public RoleActionNotFoundException() {
    super("role action not found");
  }

  public RoleActionNotFoundException(UUID roleActionId) {
    super("Role action with id "+ roleActionId + " not found");
  }
}
