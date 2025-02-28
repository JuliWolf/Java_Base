package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups.GroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Group;

@Service
public class GroupsDAO {
  @Autowired
  protected GroupRepository groupRepository;

  public Group findGroupById(UUID groupId) throws GroupNotFoundException {
    Optional<Group> group = groupRepository.findById(groupId);

    if (group.isEmpty()) {
      throw new GroupNotFoundException();
    }

    if (group.get().getIsDeleted()) {
      throw new GroupNotFoundException();
    }

    return group.get();
  }

  public List<Group> findGroupByGroupIds(List<UUID> groupIds) {
    return groupRepository.findGroupByGroupIds(groupIds);
  }
}
