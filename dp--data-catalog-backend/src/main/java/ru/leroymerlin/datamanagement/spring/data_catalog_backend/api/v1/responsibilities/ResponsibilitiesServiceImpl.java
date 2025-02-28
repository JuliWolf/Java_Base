package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models.ResponsibilityToDelete;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models.ResponsibilityWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.CollectionUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.SortUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.SourceAssetIsNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.UsersDAO;

/**
 * @author juliwolf
 */

@Service
public class ResponsibilitiesServiceImpl extends ResponsibilitiesDAO implements ResponsibilitiesService {
  private final AssetsDAO assetsDAO;

  private final UsersDAO usersDAO;

  private final RolesDAO rolesDAO;

  private final GroupsDAO groupsDAO;

  private final RelationsDAO relationsDAO;

  private final ResponsibilitiesBulkService responsibilitiesBulkService;

  public ResponsibilitiesServiceImpl (
    ResponsibilityRepository responsibilityRepository,
    RelationComponentRepository relationComponentRepository,
    EntityManager entityManager,
    AssetsDAO assetsDAO,
    UsersDAO usersDAO,
    RolesDAO rolesDAO,
    GroupsDAO groupsDAO,
    RelationsDAO relationsDAO,
    ResponsibilitiesBulkService responsibilitiesBulkService
  ) {
    super(responsibilityRepository, relationComponentRepository, entityManager);

    this.assetsDAO = assetsDAO;
    this.usersDAO = usersDAO;
    this.rolesDAO = rolesDAO;
    this.groupsDAO = groupsDAO;
    this.relationsDAO = relationsDAO;
    this.responsibilitiesBulkService = responsibilitiesBulkService;
  }

  @Override
  @Transactional
  public PostResponsibilityResponse createResponsibility (
    PostResponsibilityRequest responsibilityRequest,
    User user
  )
    throws
    AssetNotFoundException,
    RoleNotFoundException,
    UserNotFoundException
  {
    ResponsibleType responsibleType = ResponsibleType.valueOf(responsibilityRequest.getResponsible_type());

    Asset asset = assetsDAO.findAssetById(responsibilityRequest.getAsset_id());
    Role role = rolesDAO.findRoleById(responsibilityRequest.getRole_id());
    User responsibleUser;
    Group responsibleGroup;

    if (responsibleType.equals(ResponsibleType.USER)) {
      responsibleUser = usersDAO.findUserById(responsibilityRequest.getResponsible_id());
    } else {
      responsibleUser = null;
    }

    if (responsibleType.equals(ResponsibleType.GROUP)) {
      responsibleGroup = groupsDAO.findGroupById(responsibilityRequest.getResponsible_id());
    } else {
      responsibleGroup = null;
    }

    Responsibility parentResponsibility = responsibilityRepository.save(new Responsibility(
      responsibleUser,
      responsibleGroup,
      asset,
      role,
      responsibleType,
      user
    ));

    createChildResponsibilitiesForAsset(Map.of(asset.getAssetId(), List.of(parentResponsibility)), user);

    return new PostResponsibilityResponse(
      parentResponsibility.getResponsibilityId(),
      asset.getAssetId(),
      role.getRoleId(),
      responsibleType,
      responsibleType.equals(ResponsibleType.USER) ? responsibleUser.getUserId() : responsibleGroup.getGroupId(),
      parentResponsibility.getCreatedOn(),
      user.getUserId()
    );
  }

  @Override
  @Transactional
  public List<PostResponsibilityResponse> createResponsibilitiesBulk (
    List<PostResponsibilityRequest> responsibilitiesRequest,
    User user
  ) throws
    UserNotFoundException,
    RoleNotFoundException,
    AssetNotFoundException,
    GroupNotFoundException
  {
    Set<UUID> assetsIds = new HashSet<>();
    Set<UUID> rolesIds = new HashSet<>();
    Set<UUID> usersIds = new HashSet<>();
    Set<UUID> groupsIds = new HashSet<>();

    responsibilitiesRequest.forEach(request -> {
      if (
        request.getRole_id() == null ||
        request.getAsset_id() == null ||
        request.getResponsible_id() == null ||
        StringUtils.isEmpty(request.getResponsible_type())
      ) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      ResponsibleType responsibleType = ResponsibleType.valueOf(request.getResponsible_type());
      if (responsibleType.equals(ResponsibleType.USER)) {
        usersIds.add(request.getResponsible_id());
      }

      if (responsibleType.equals(ResponsibleType.GROUP)) {
        groupsIds.add(request.getResponsible_id());
      }

      assetsIds.add(request.getAsset_id());
      rolesIds.add(request.getRole_id());
    });

    Map<UUID, Asset> assetsMap = findAssets(assetsIds);
    checkSourceAssetIds(assetsIds);

    Map<UUID, User> usersMap = findUsers(usersIds);
    Map<UUID, Role> rolesMap = findRoles(rolesIds);
    Map<UUID, Group> groupsMap = findGroups(groupsIds);

    try {
      return responsibilitiesBulkService.createResponsibilitiesBulk(
        responsibilitiesRequest,
        assetsMap,
        usersMap,
        rolesMap,
        groupsMap,
        user
      );
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      throw new ResponsibilityAlreadyExistsException(dataIntegrityViolationException);
    }
  }

  @Override
  public GetResponsibilityResponse getResponsibilityById (UUID responsibilityId) throws ResponsibilityNotFoundException {
    Optional<ResponsibilityWithConnectedValues> optionalResponsibility = responsibilityRepository.findResponsibilityById(responsibilityId);

    if (optionalResponsibility.isEmpty()) {
      throw new ResponsibilityNotFoundException();
    }

    return mapResponsibility(optionalResponsibility.get());
  }

  @Override
  public GetResponsibilitiesResponse getResponsibilitiesByParams (
    List<UUID> assetIds,
    List<UUID> roleIds,
    List<UUID> userIds,
    List<UUID> groupIds,
    List<UUID> assetTypeIds,
    List<UUID> lifecycleStatusIds,
    List<UUID> stewardshipStatusIds,
    Boolean inheritedFlag,
    SortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  ) throws IllegalArgumentException {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<ResponsibilityWithConnectedValues> responsibilities = responsibilityRepository.findAllByParamsWithJoinedTablesPageable(
      assetIds != null ? assetIds.size() : 0,
      assetIds,
      roleIds != null ? roleIds.size() : 0,
      roleIds,
      userIds != null ? userIds.size() : 0,
      userIds,
      groupIds != null ? groupIds.size() : 0,
      groupIds,
      assetTypeIds != null ? assetTypeIds.size() : 0,
      assetTypeIds,
      lifecycleStatusIds != null ? lifecycleStatusIds.size() : 0,
      lifecycleStatusIds,
      stewardshipStatusIds != null ? stewardshipStatusIds.size() : 0,
      stewardshipStatusIds,
      inheritedFlag,
      PageRequest.of(pageNumber, pageSize, getSorting(sortField != null ? sortField : SortField.ROLE_NAME, sortOrder))
    );

    List<GetResponsibilityResponse> responseList = responsibilities.stream()
      .map(this::mapResponsibility)
      .toList();

    return new GetResponsibilitiesResponse(
      responsibilities.getTotalElements(),
      pageSize,
      pageNumber,
      responseList
    );
  }

  @Override
  @Transactional
  public void deleteResponsibilityById (UUID responsibilityId, User user) throws ResponsibilityNotFoundException, ResponsibilityIsInheritedException {
    Responsibility responsibility = findResponsibilityById(responsibilityId, false);

    if (responsibility.getInheritedFlag()) {
      throw new ResponsibilityIsInheritedException();
    }

    deleteAllByParentResponsibilityId(List.of(responsibilityId), user);

    responsibility.setIsDeleted(true);
    responsibility.setDeletedBy(user);
    responsibility.setDeletedOn(new Timestamp(System.currentTimeMillis()));
  }

  @Override
  @Transactional
  public void deleteResponsibilitiesBulk (
    List<UUID> responsibilitiesRequest,
    User user
  )
    throws
    ResponsibilityNotFoundException,
    ResponsibilityIsInheritedException
  {
    UUID firstDuplicate = CollectionUtils.findFirstDuplicate(responsibilitiesRequest);
    if (firstDuplicate != null) {
      throw new DuplicateValueInRequestException("Duplicate responsibility_id in request", Map.of("responsibility_id", firstDuplicate));
    }

    List<ResponsibilityToDelete> responsibilityToDelete = responsibilityRepository.findResponsibilitiesToDeleteByResponsibilitiesIds(responsibilitiesRequest);
    Set<UUID> responsibilitiesSet = responsibilityToDelete.stream().map(ResponsibilityToDelete::getResponsibilityId).collect(Collectors.toSet());
    if (responsibilitiesSet.size() != responsibilitiesRequest.size()) {
      UUID responsibilityId = CollectionUtils.findFirstNotFoundValue(new HashSet<>(responsibilitiesRequest), responsibilitiesSet);

      throw new ResponsibilityNotFoundException(Map.of("responsibility_id", responsibilityId));
    }

    ResponsibilityToDelete responsibility = responsibilityToDelete.stream().filter(ResponsibilityToDelete::getInheritedFlag).findFirst().orElse(null);
    if (responsibility != null) {
      throw new ResponsibilityIsInheritedException(Map.of("responsibility_id", responsibility.getResponsibilityId()));
    }

    ResponsibilityToDelete sourceResponsibility = responsibilityToDelete.stream().filter(resp -> resp.getRelationId() != null).findFirst().orElse(null);

    if (sourceResponsibility != null) {
      throw new SourceAssetIsNotAllowedException(Map.of("responsibility_id", sourceResponsibility.getResponsibilityId()));
    }

    deleteAllByParentResponsibilityId(responsibilitiesRequest, user);

    responsibilityRepository.deleteAllByResponsibilityIds(responsibilitiesRequest, user.getUserId());
  }

  private GetResponsibilityResponse mapResponsibility (ResponsibilityWithConnectedValues responsibility) {
    return new GetResponsibilityResponse(
      responsibility.getResponsibilityId(),
      responsibility.getResponsibleId(),
      responsibility.getResponsibleName(),
      responsibility.getResponsibleFullName(),
      responsibility.getResponsibleType(),
      responsibility.getRoleId(),
      responsibility.getRoleName(),
      responsibility.getAssetId(),
      responsibility.getAssetName(),
      responsibility.getAssetDisplayName(),
      responsibility.getAssetTypeId(),
      responsibility.getAssetTypeName(),
      responsibility.getStewardshipStatusId(),
      responsibility.getStewardshipStatusName(),
      responsibility.getLifecycleStatusId(),
      responsibility.getLifecycleStatusName(),
      responsibility.getInheritedFlag(),
      responsibility.getParentResponsibilityId(),
      responsibility.getRelationId(),
      responsibility.getCreatedOn(),
      responsibility.getCreatedBy()
    );
  }

  private Map<UUID, Asset> findAssets (Set<UUID> assetsIds) {
    List<Asset> assets = assetsDAO.findAllByAssetIds(assetsIds.stream().toList());

    Map<UUID, Asset> assetsMap = assets.stream()
      .collect(Collectors.toMap(Asset::getAssetId, value -> value));

    if (assets.size() != assetsIds.size()) {
      UUID absentAssetId = CollectionUtils.findFirstNotFoundValue(assetsIds, assetsMap.keySet());

      PostResponsibilityRequest request = new PostResponsibilityRequest();
      request.setAsset_id(absentAssetId);

      throw new AssetNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return assetsMap;
  }

  private void checkSourceAssetIds (Set<UUID> assetsIds) {
    List<UUID> sourceAssetIdsByAssetIds = relationsDAO.findAllSourceAssetIdsByAssetIds(assetsIds.stream().toList());

    if (sourceAssetIdsByAssetIds.isEmpty()) return;

    PostResponsibilityRequest request = new PostResponsibilityRequest();
    request.setAsset_id(sourceAssetIdsByAssetIds.get(0));

    throw new SourceAssetIsNotAllowedException(ObjectUtils.convertObjectToMap(request));
  }

  private Map<UUID, Role> findRoles (Set<UUID> rolesIds) {
    List<Role> roles = rolesDAO.findAllByRoleIds(rolesIds.stream().toList());

    Map<UUID, Role> rolesMap = roles.stream()
      .collect(Collectors.toMap(Role::getRoleId, value -> value));

    if (roles.size() != rolesIds.size()) {
      UUID absentRoleId = CollectionUtils.findFirstNotFoundValue(rolesIds, rolesMap.keySet());

      PostResponsibilityRequest request = new PostResponsibilityRequest();
      request.setRole_id(absentRoleId);

      throw new RoleNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return rolesMap;
  }

  private Map<UUID, Group> findGroups (Set<UUID> groupsIds) {
    List<Group> groups = groupsDAO.findGroupByGroupIds(groupsIds.stream().toList());

    Map<UUID, Group> groupsMap = groups.stream()
      .collect(Collectors.toMap(Group::getGroupId, value -> value));

    if (groups.size() != groupsIds.size()) {
      UUID absentGroupId = CollectionUtils.findFirstNotFoundValue(groupsIds, groupsMap.keySet());

      PostResponsibilityRequest request = new PostResponsibilityRequest();
      request.setResponsible_id(absentGroupId);
      request.setResponsible_type(ResponsibleType.GROUP.name());

      throw new GroupNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return groupsMap;
  }

  private Map<UUID, User> findUsers (Set<UUID> usersIds) {
    List<User> users = usersDAO.findUsersByUserIds(usersIds.stream().toList());

    Map<UUID, User> usersMap = users.stream()
      .collect(Collectors.toMap(User::getUserId, value -> value));

    if (users.size() != usersIds.size()) {
      UUID absentUserId = CollectionUtils.findFirstNotFoundValue(usersIds, usersMap.keySet());

      PostResponsibilityRequest request = new PostResponsibilityRequest();
      request.setResponsible_id(absentUserId);
      request.setResponsible_type(ResponsibleType.USER.name());

      throw new UserNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return usersMap;
  }

  private Sort getSorting (SortField sortField, SortOrder sortType) {
    return switch (sortField) {
      case RESPONSIBLE_FULLNAME -> Sort.by(
        SortUtils.getSortOrder(sortType, SortField.RESPONSIBLE_NAME.getValue(), SortField.ROLE_NAME.getValue()),
        SortUtils.getSortOrder(sortType, SortField.RESPONSIBLE_LAST_NAME.getValue(), SortField.ROLE_NAME.getValue())
      );
      default -> SortUtils.getSort(sortType, sortField.getValue(), SortField.ROLE_NAME.getValue());
    };
  }
}
