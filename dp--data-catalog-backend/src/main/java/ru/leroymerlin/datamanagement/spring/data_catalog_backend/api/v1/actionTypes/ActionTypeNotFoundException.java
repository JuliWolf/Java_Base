package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class ActionTypeNotFoundException extends RuntimeException {
  public ActionTypeNotFoundException() {
    super("action type not found");
  }

  public ActionTypeNotFoundException(UUID actionTypeId) {
    super("Action type with id " + actionTypeId + " not found");
  }
}
