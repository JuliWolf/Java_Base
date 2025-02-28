package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.UserGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.get.GetUserGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.post.PostUserGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

public interface UserGroupsService {
  UserGroupResponse createUserGroup (
    PostUserGroupRequest request,
    User user
  ) throws IllegalArgumentException, UserNotFoundException, GroupNotFoundException;

  GetUserGroupsResponse getUserGroupsByUserIdAndRoleId (UUID userId, UUID roleId, Integer pageNumber, Integer pageSize);

  void deleteUserGroupById (UUID userGroupId, User user);
}
