package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Group;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.UserGroup;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.UserGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.get.GetUserGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.get.GetUserGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.post.PostUserGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.UsersDAO;

/**
 * @author JuliWolf
 */
@Service
public class UserGroupsServiceImpl extends UserGroupsDAO implements UserGroupsService {
  @Autowired
  private UsersDAO usersDAO;

  @Autowired
  private GroupsDAO groupsDAO;

  @Override
  public UserGroupResponse createUserGroup (
    PostUserGroupRequest request,
    User user
  ) throws IllegalArgumentException, UserNotFoundException, GroupNotFoundException {
    User foundUser = usersDAO.findUserById(UUID.fromString(request.getUser_id()));
    Group foundGroup = groupsDAO.findGroupById(UUID.fromString(request.getGroup_id()));

    UserGroup userGroup = userGroupRepository.save(new UserGroup(
      foundUser,
      foundGroup,
      user
    ));

    return new UserGroupResponse(
      userGroup.getUserGroupId(),
      foundUser.getUserId(),
      foundUser.getUsername(),
      foundGroup.getGroupId(),
      foundGroup.getGroupName(),
      userGroup.getAddedOn(),
      user.getUserId()
    );
  }

  @Override
  public GetUserGroupsResponse getUserGroupsByUserIdAndRoleId (UUID userId, UUID roleId, Integer pageNumber, Integer pageSize) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<UserGroup> userGroups = userGroupRepository.findAllByUserIdAndGroupIdPageable(
      userId,
      roleId,
      PageRequest.of(pageNumber, pageSize, Sort.by("userGroupId").ascending())
    );

    List<GetUserGroupResponse> userGroupResponseList = userGroups.stream().map(userGroup ->
      new GetUserGroupResponse(
        userGroup.getUserGroupId(),
        userGroup.getUser().getUserId(),
        userGroup.getUser().getUsername(),
        userGroup.getUser().getFirstName(),
        userGroup.getUser().getLastName(),
        userGroup.getGroup().getGroupId(),
        userGroup.getGroup().getGroupName(),
        userGroup.getAddedOn(),
        userGroup.getAddedBy().getUserId()
      )
    ).toList();

    return new GetUserGroupsResponse(
      userGroups.getTotalElements(),
      pageSize,
      pageNumber,
      userGroupResponseList
    );
  }

  @Override
  public void deleteUserGroupById (UUID userGroupId, User user) throws UserGroupNotFoundException {
    UserGroup foundUserGroup = findUserGroupById(userGroupId);

    foundUserGroup.setIsDeleted(true);
    foundUserGroup.setDeletedBy(user);
    foundUserGroup.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    userGroupRepository.save(foundUserGroup);
  }
}
