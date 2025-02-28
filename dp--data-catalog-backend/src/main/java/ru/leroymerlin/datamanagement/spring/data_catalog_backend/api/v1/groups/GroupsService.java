package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get.GetGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get.GetGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupResponse;

public interface GroupsService {
  PostGroupResponse createGroup (PostGroupRequest groupRequest, User user);

  GetGroupsResponse getGroupsByParams (String name, String description, Integer pageNumber, Integer pageSize);

  GetGroupResponse getGroupById (UUID groupId) throws GroupNotFoundException;

  void deleteGroupById (UUID groupId, User user) throws GroupNotFoundException;
}
