package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.role;

import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;

/**
 * @author juliwolf
 */

public interface RoleWithUsageCountResponse {
  Role getRole();

  Long getResponsibilitiesCount();

  Long getGlobalResponsibilitiesCount();

  default Long getUsageCount() {
    return (long) getResponsibilitiesCount() + getGlobalResponsibilitiesCount();
  };
}
