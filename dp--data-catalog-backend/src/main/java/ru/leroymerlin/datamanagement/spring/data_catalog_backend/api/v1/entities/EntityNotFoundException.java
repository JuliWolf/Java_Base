package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities;

import java.util.UUID;
import lombok.Getter;

/**
 * @author JuliWolf
 */
@Getter
public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException() {
    super("entity not found");
  }

  public EntityNotFoundException(UUID entityId) {
    super("Entity with id " + entityId + " not found");
  }
}
