package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.CreateGlobalResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.PostGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

public interface GlobalResponsibilitiesService {
  PostGlobalResponsibilityResponse createGlobalResponsibility (
    CreateGlobalResponsibilityRequest request,
    User user
  ) throws
    RoleNotFoundException,
    GroupNotFoundException,
    UserNotFoundException,
    IllegalArgumentException;

  GetGlobalResponsibilitiesResponse getGlobalResponsibilitiesByParams (
    UUID roleId,
    UUID responsibleId,
    String responsibleType,
    SortField sortField,
    SortOrder sortType,
    Integer pageNumber,
    Integer pageSize
  );

  GetGlobalResponsibilityResponse getGlobalResponsibilityById(UUID globalResponsibilityId) throws GlobalResponsibilityNotFoundException;

  void deleteGlobalResponsibilityById (UUID globalResponsibilityId, User user) throws GlobalResponsibilityNotFoundException;
}
