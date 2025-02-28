package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models;

/**
 * @author juliwolf
 */

public interface RoleUsageCount {
  Long getResponsibilitiesUsageCount();

  Long getGlobalResponsibilitiesUsageCount();

  default Long getUsageCount () {
    return getResponsibilitiesUsageCount() + getGlobalResponsibilitiesUsageCount();
  }
}
