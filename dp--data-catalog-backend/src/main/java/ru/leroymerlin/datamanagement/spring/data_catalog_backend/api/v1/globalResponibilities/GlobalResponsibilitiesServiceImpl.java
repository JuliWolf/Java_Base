package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.models.GlobalResponsibilityWithRoleAndResponsible;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.GlobalResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Group;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.SortUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityResponsibleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.CreateGlobalResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.PostGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.UsersDAO;

/**
 * @author JuliWolf
 */
@Service
public class GlobalResponsibilitiesServiceImpl extends GlobalResponsibilitiesDAO implements GlobalResponsibilitiesService {
  @Autowired
  private RoleActionCachingService roleActionCachingService;

  @Autowired
  private RolesDAO rolesDAO;

  @Autowired
  private UsersDAO usersDAO;

  @Autowired
  private GroupsDAO groupsDAO;

  @Override
  public PostGlobalResponsibilityResponse createGlobalResponsibility (
    CreateGlobalResponsibilityRequest request,
    User user
  ) throws RoleNotFoundException, GroupNotFoundException, UserNotFoundException, IllegalArgumentException {
    Role role = rolesDAO.findRoleById(UUID.fromString(request.getRole_id()));
    User responsibleUser = null;
    Group responsibleGroup = null;
    UUID responsibleId = UUID.fromString(request.getResponsible_id());

    switch (request.getResponsible_type()) {
      case USER -> responsibleUser = usersDAO.findUserById(responsibleId);
      case GROUP -> responsibleGroup = groupsDAO.findGroupById(responsibleId);
      default -> throw new GlobalResponsibilityResponsibleNotFoundException();
    }

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(
      responsibleUser,
      responsibleGroup,
      role,
      request.getResponsible_type(),
      user
    ));

    clearRoleActionsCache(role.getRoleId(), responsibleId);

    return new PostGlobalResponsibilityResponse(
      globalResponsibility.getGlobalResponsibilityId(),
      globalResponsibility.getResponsibleId(),
      globalResponsibility.getResponsibleType(),
      role.getRoleId(),
      globalResponsibility.getCreatedOn(),
      globalResponsibility.getCreatedByUUID()
    );
  }

  @Override
  public GetGlobalResponsibilitiesResponse getGlobalResponsibilitiesByParams (
    UUID roleId,
    UUID responsibleId,
    String responsibleType,
    SortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    ResponsibleType responsibleTypeValue = StringUtils.isNotEmpty(responsibleType) ? ResponsibleType.valueOf(responsibleType) : null;

    Page<GlobalResponsibilityWithRoleAndResponsible> globalResponsibilities = globalResponsibilitiesRepository
      .findAllByRoleIdResponsibleIdAndResponsibilityTypePageable(
        roleId,
        responsibleId,
        responsibleTypeValue,
        PageRequest.of(pageNumber, pageSize, getSorting(sortField != null ? sortField : SortField.ROLE_NAME, sortOrder, responsibleTypeValue))
      );

    List<GetGlobalResponsibilityResponse> getGlobalResponsibilityList = globalResponsibilities.stream()
      .map(responsibility ->
        new GetGlobalResponsibilityResponse(
          responsibility.getGlobalResponsibilityId(),
          responsibility.getResponsibleId(),
          responsibility.getResponsibleName(),
          responsibility.getResponsibleType(),
          responsibility.getRoleId(),
          responsibility.getRoleName(),
          responsibility.getRoleDescription(),
          responsibility.getCreatedOn(),
          responsibility.getCreatedBy()
        )
      ).toList();

    return new GetGlobalResponsibilitiesResponse(
      globalResponsibilities.getTotalElements(),
      pageSize,
      pageNumber,
      getGlobalResponsibilityList
    );
  }

  @Override
  public GetGlobalResponsibilityResponse getGlobalResponsibilityById(UUID globalResponsibilityId) throws GlobalResponsibilityNotFoundException {
    Optional<GlobalResponsibilityWithRoleAndResponsible> optionalGlobalResponsibility = globalResponsibilitiesRepository.findGlobalResponsibilityById(globalResponsibilityId);

    if (optionalGlobalResponsibility.isEmpty()) {
      throw new GlobalResponsibilityNotFoundException(globalResponsibilityId);
    }

    GlobalResponsibilityWithRoleAndResponsible globalResponsibility = optionalGlobalResponsibility.get();

    return new GetGlobalResponsibilityResponse(
      globalResponsibility.getGlobalResponsibilityId(),
      globalResponsibility.getResponsibleId(),
      globalResponsibility.getResponsibleName(),
      globalResponsibility.getResponsibleType(),
      globalResponsibility.getRoleId(),
      globalResponsibility.getRoleName(),
      globalResponsibility.getRoleDescription(),
      globalResponsibility.getCreatedOn(),
      globalResponsibility.getCreatedBy()
    );
  }

  @Override
  public void deleteGlobalResponsibilityById (UUID globalResponsibilityId, User user) throws GlobalResponsibilityNotFoundException {
    GlobalResponsibility foundGlobalResponsibility = findGlobalResponsibilityById(globalResponsibilityId);

    foundGlobalResponsibility.setIsDeleted(true);
    foundGlobalResponsibility.setDeletedBy(user);
    foundGlobalResponsibility.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    globalResponsibilitiesRepository.save(foundGlobalResponsibility);

    clearRoleActionsCache(foundGlobalResponsibility.getRole().getRoleId(), foundGlobalResponsibility.getResponsibleId());
  }

  private void clearRoleActionsCache (UUID roleId, UUID responsibleId) {
    roleActionCachingService.evictByRoleId(roleId);
    roleActionCachingService.evictByValueInKey(roleId.toString());
    roleActionCachingService.evictByValueInKey(responsibleId.toString());
  }

  private Sort getSorting (SortField sortField, SortOrder sortType, ResponsibleType responsibleTypeValue) {
    Sort sort;

    switch (sortField) {
      case RESPONSIBLE_NAME -> {
        if (responsibleTypeValue == null) {
          sort = Sort.by(
            SortUtils.getSortOrder(sortType, "u.username", SortField.ROLE_NAME.getValue()),
            SortUtils.getSortOrder(sortType, "g.groupName", SortField.ROLE_NAME.getValue())
          );

          break;
        }

        if (ResponsibleType.GROUP.equals(responsibleTypeValue)) {
          sort = SortUtils.getSort(sortType, "g.groupName", SortField.ROLE_NAME.getValue());
          break;
        }

        sort = SortUtils.getSort(sortType, "u.username", SortField.ROLE_NAME.getValue());
      }
      default -> sort = SortUtils.getSort(sortType, sortField.getValue(), SortField.ROLE_NAME.getValue());
    };

    return sort;
  }
}
