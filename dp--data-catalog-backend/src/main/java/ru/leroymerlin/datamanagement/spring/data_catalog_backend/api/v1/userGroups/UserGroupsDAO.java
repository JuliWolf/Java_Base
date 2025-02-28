package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.UserGroup;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userGroups.UserGroupRepository;

@Service
public class UserGroupsDAO {
  @Autowired
  protected UserGroupRepository userGroupRepository;

  public UserGroup findUserGroupById(UUID userGroupId) throws UserGroupNotFoundException {
    Optional<UserGroup> userGroup = userGroupRepository.findById(userGroupId);

    if (userGroup.isEmpty()) {
      throw new UserGroupNotFoundException(userGroupId);
    }

    if (userGroup.get().getIsDeleted()) {
      throw new UserGroupNotFoundException(userGroupId);
    }

    return userGroup.get();
  }

  public void deleteAllByParams (UUID userId, UUID groupId, User user) {
    userGroupRepository.deleteByParams(userId, groupId, user.getUserId());
  }
}
