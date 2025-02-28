package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class GlobalResponsibilityNotFoundException extends RuntimeException {
  public GlobalResponsibilityNotFoundException() {
    super("global responsibility not found");
  }

  public GlobalResponsibilityNotFoundException(UUID globalResponsibilityId) {
    super("Global responsibility with id " + globalResponsibilityId + " not found");
  }
}
