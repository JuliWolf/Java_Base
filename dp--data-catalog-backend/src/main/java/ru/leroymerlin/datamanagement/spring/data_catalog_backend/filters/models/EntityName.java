package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.models;

import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;

/**
 * @author juliwolf
 */

public record EntityName (
  EntityNameType entityNameTypeFromRequest,

  String method,

  EntityNameType additionalEntityType
) {}
