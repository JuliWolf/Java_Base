package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Group;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get.GetGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get.GetGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.UserGroupsDAO;

/**
 * @author JuliWolf
 */
@Service
public class GroupsServiceImpl extends GroupsDAO implements GroupsService {
  @Autowired
  private UserGroupsDAO userGroupsDAO;

  @Autowired
  private GlobalResponsibilitiesDAO globalResponsibilitiesDAO;

  @Autowired
  private ResponsibilitiesDAO responsibilitiesDAO;

  @Override
  public PostGroupResponse createGroup (PostGroupRequest groupRequest, User user) {
    Group group = groupRepository.save(new Group(
      groupRequest.getGroup_name(),
      groupRequest.getGroup_description(),
      groupRequest.getGroup_email(),
      groupRequest.getGroup_messenger(),
      user
    ));

    return new PostGroupResponse(
      group.getGroupId(),
      group.getGroupName(),
      group.getGroupDescription(),
      group.getGroupEmail(),
      group.getGroupMessenger(),
      group.getCreatedOn(),
      group.getCreatedByUUID()
    );
  }

  @Override
  public GetGroupsResponse getGroupsByParams (
    String name,
    String description,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<Group> groups = groupRepository.findAllByGroupNameAndDescriptionPageable(
      name,
      description,
      PageRequest.of(pageNumber, pageSize, Sort.by("groupName").ascending())
    );

    List<GetGroupResponse> groupsList = groups.stream().map(group -> {
      UUID lastModifiedBy = group.getModifiedBy() != null ? group.getModifiedBy().getUserId() : null;

      return new GetGroupResponse(
        group.getGroupId(),
        group.getGroupName(),
        group.getGroupDescription(),
        group.getGroupEmail(),
        group.getGroupMessenger(),
        group.getCreatedOn(),
        group.getCreatedByUUID(),
        group.getLastModifiedOn(),
        lastModifiedBy
      );
    }).collect(Collectors.toList());

    return new GetGroupsResponse(
      groups.getTotalElements(),
      pageSize,
      pageNumber,
      groupsList
    );
  }

  @Override
  public GetGroupResponse getGroupById (UUID groupId) throws GroupNotFoundException {
    Group group = findGroupById(groupId);

    UUID lastModifiedBy = group.getModifiedBy() != null ? group.getModifiedBy().getUserId() : null;

    return new GetGroupResponse(
      group.getGroupId(),
      group.getGroupName(),
      group.getGroupDescription(),
      group.getGroupEmail(),
      group.getGroupMessenger(),
      group.getCreatedOn(),
      group.getCreatedByUUID(),
      group.getLastModifiedOn(),
      lastModifiedBy
    );
  }

  @Override
  @Transactional
  public void deleteGroupById (UUID groupId, User user) throws GroupNotFoundException {
    Group foundGroup = findGroupById(groupId);

    userGroupsDAO.deleteAllByParams(null, groupId, user);
    responsibilitiesDAO.deleteAllByParams(null, null, null, groupId, null, null, user);
    globalResponsibilitiesDAO.deleteAllByGroupId(groupId, user);

    foundGroup.setIsDeleted(true);
    foundGroup.setDeletedBy(user);
    foundGroup.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    groupRepository.save(foundGroup);
  }
}
