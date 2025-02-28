package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class RoleNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public RoleNotFoundException(UUID roleId) {
    super("Role with id " + roleId + " not found");
  }

  public RoleNotFoundException() {
    super("role not found");
  }

  public RoleNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
