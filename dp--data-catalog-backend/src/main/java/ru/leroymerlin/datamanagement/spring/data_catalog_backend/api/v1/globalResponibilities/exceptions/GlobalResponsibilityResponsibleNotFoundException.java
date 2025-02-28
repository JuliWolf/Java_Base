package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class GlobalResponsibilityResponsibleNotFoundException extends RuntimeException {
  public GlobalResponsibilityResponsibleNotFoundException() {
    super("global responsibility responsible not found");
  }

  public GlobalResponsibilityResponsibleNotFoundException(UUID globalResponsibilityResponsibleId) {
    super("Global responsibility responsible with id " + globalResponsibilityResponsibleId + " not found");
  }
}
