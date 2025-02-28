package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class RoleActionObjectNotFoundException extends RuntimeException {
  public RoleActionObjectNotFoundException() {
    super("role action object not found");
  }

  public RoleActionObjectNotFoundException(UUID roleActionObjectId) {
    super("Role action object with id "+ roleActionObjectId + " not found");
  }
}
