package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

/**
 * @author juliwolf
 */

public interface ResponsibilitiesService {
  PostResponsibilityResponse createResponsibility (
    PostResponsibilityRequest responsibilityRequest,
    User user
  ) throws
    UserNotFoundException,
    RoleNotFoundException,
    AssetNotFoundException,
    GroupNotFoundException;

  GetResponsibilityResponse getResponsibilityById (UUID responsibilityId) throws ResponsibilityNotFoundException;

  GetResponsibilitiesResponse getResponsibilitiesByParams (
    List<UUID> assetIds,
    List<UUID> roleIds,
    List<UUID> userIds,
    List<UUID> groupIds,
    List<UUID> assetTypeIds,
    List<UUID> lifecycleStatusIds,
    List<UUID> stewardshipStatusIds,
    Boolean inheritedFlag,
    SortField sortField,
    SortOrder sortType,
    Integer pageNumber,
    Integer pageSize
  ) throws IllegalArgumentException;

  void deleteResponsibilityById (UUID responsibilityId, User user) throws ResponsibilityNotFoundException, ResponsibilityIsInheritedException;

  List<PostResponsibilityResponse> createResponsibilitiesBulk (
    List<PostResponsibilityRequest> responsibilitiesRequest,
    User user
  ) throws
    UserNotFoundException,
    RoleNotFoundException,
    AssetNotFoundException,
    GroupNotFoundException;

  void deleteResponsibilitiesBulk (List<UUID> responsibilitiesRequest, User user) throws ResponsibilityNotFoundException, ResponsibilityIsInheritedException;
}
