package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.UsernameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.structUnits.StructUnitsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.structUnits.exceptions.StructUnitNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.SubscriptionsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.UserGroupsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SearchField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserRoleResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUsersResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostOrPatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.StructUnit;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.models.UserRoleResponsibilityCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.models.UserWithLanguage;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.CollectionUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.SortUtils;

/**
 * @author JuliWolf
 */
@Service
public class UsersServiceImpl extends UsersDAO implements UsersService {
 private final RoleActionCachingService roleActionCachingService;

  private final UserGroupsDAO userGroupsDAO;

  private final ResponsibilitiesDAO responsibilitiesDAO;

  private final LanguageService languageService;

  private final SubscriptionsDAO subscriptionsDAO;

  private final StructUnitsDAO structUnitsDAO;

  private final BulkUsersService bulkUsersService;

  private final UsersUtilsService usersUtilsService;

  public UsersServiceImpl (
    UserRepository userRepository,
    UserGroupsDAO userGroupsDAO,
    StructUnitsDAO structUnitsDAO,
    LanguageService languageService,
    SubscriptionsDAO subscriptionsDAO,
    ResponsibilitiesDAO responsibilitiesDAO,
    RoleActionCachingService roleActionCachingService,
    GlobalResponsibilitiesDAO globalResponsibilitiesDAO,
    BulkUsersService bulkUsersService,
    UsersUtilsService usersUtilsService
  ) {
    super(
      userRepository,
      globalResponsibilitiesDAO
    );

    this.userGroupsDAO = userGroupsDAO;
    this.structUnitsDAO = structUnitsDAO;
    this.languageService = languageService;
    this.subscriptionsDAO = subscriptionsDAO;
    this.responsibilitiesDAO = responsibilitiesDAO;
    this.roleActionCachingService = roleActionCachingService;
    this.bulkUsersService = bulkUsersService;
    this.usersUtilsService = usersUtilsService;
  }

  @Override
  @Transactional
  public PostUserResponse createUser (
    PostOrPatchUserRequest userRequest,
    User user
  ) throws
    StructUnitNotFoundException,
    InvalidFieldLengthException,
    DataIntegrityViolationException
  {
    validateFieldsLength(userRequest);

    Language ru = languageService.getLanguage("ru");
    StructUnit structUnit = StringUtils.isNotEmpty(userRequest.getStruct_unit_id())
      ? findStructUnitById(userRequest.getStruct_unit_id())
      : null;

    UserType userType = usersUtilsService.detectUserType(userRequest.getUsername(), userRequest.getEmail());

    User createdUser = userRepository.save(new User(
      userRequest.getUsername(),
      userRequest.getEmail(),
      userRequest.getFirst_name(),
      userRequest.getLast_name(),
      SourceType.API,
      structUnit,
      userType,
      userRequest.getUser_work_status() == null ? UserWorkStatus.ACTIVE : userRequest.getUser_work_status(),
      userRequest.getUser_photo_link(),
      ru,
      user
    ));

    usersUtilsService.createUserHistory(createdUser, MethodType.POST);
    usersUtilsService.createDefaultGlobalResponsibility(createdUser, user);

    return new PostUserResponse(
      createdUser.getUserId(),
      createdUser.getUsername(),
      createdUser.getEmail(),
      createdUser.getFirstName(),
      createdUser.getLastName(),
      createdUser.getSource(),
      createdUser.getBossKPid(),
      createdUser.getStructUnit() != null ? createdUser.getStructUnit().getStructUnitId() : null,
      createdUser.getUserType(),
      createdUser.getUserWorkStatus(),
      usersUtilsService.parsePhotoLinks(userRequest.getUser_photo_link()),
      ru.getLanguage(),
      createdUser.getCreatedOn(),
      user.getUserId()
    );
  }

  @Override
  public List<PostUserResponse> createUsersBulk (
    List<PostOrPatchUserRequest> userRequests,
    User user
  ) throws
    StructUnitNotFoundException,
    InvalidFieldLengthException,
    UsernameAlreadyExistsException,
    DuplicateValueInRequestException,
    SomeRequiredFieldsAreEmptyException
  {
    Set<UUID> structUnitIds = new HashSet<>();
    Set<String> usernamesSet = new HashSet<>();

    userRequests.forEach(request -> {
      if (
        StringUtils.isEmpty(request.getUsername()) ||
        request.getUser_work_status() == null
      ) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      validateFieldsLength(request);

      if (StringUtils.isNotEmpty(request.getStruct_unit_id())) {
        structUnitIds.add(UUID.fromString(request.getStruct_unit_id()));
      }

      if (usernamesSet.contains(request.getUsername())) {
        throw new DuplicateValueInRequestException("Duplicate username in request", ObjectUtils.convertObjectToMap(request));
      }

      usernamesSet.add(request.getUsername());
    });

    Map<UUID, StructUnit> structUnitsMap = findStructUnits(structUnitIds);

    try {
      return bulkUsersService.createUsers(
        userRequests,
        structUnitsMap,
        user
      );
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      throw new UsernameAlreadyExistsException(dataIntegrityViolationException);
    }
  }

  @Override
  @Transactional
  public PatchUserResponse updateUser (
    UUID userId,
    PostOrPatchUserRequest userRequest,
    User user
  ) throws
    UserNotFoundException,
    InvalidFieldLengthException,
    StructUnitNotFoundException
  {
    User foundUser = findUserById(userId);

    validateFieldsLength(userRequest);

    StructUnit structUnit = StringUtils.isNotEmpty(userRequest.getStruct_unit_id())
      ? findStructUnitById(userRequest.getStruct_unit_id())
      : foundUser.getStructUnit();

    return bulkUsersService.updateUser(userRequest, foundUser, structUnit, user);
  }

  @Override
  public List<PatchUserResponse> updateUsersBulk (
    List<PatchUserRequest> userRequests,
    User user
  ) throws
    UserNotFoundException,
    InvalidFieldLengthException,
    StructUnitNotFoundException,
    UsernameAlreadyExistsException,
    SomeRequiredFieldsAreEmptyException
  {
    Set<UUID> userIds = new HashSet<>();
    Set<UUID> structUnitIds = new HashSet<>();
    Set<String> usernamesSet = new HashSet<>();

    userRequests.forEach(request -> {
      if (
        request.getUser_work_status() == null &&
        StringUtils.isEmpty(request.getEmail()) &&
        StringUtils.isEmpty(request.getUsername()) &&
        StringUtils.isEmpty(request.getFirst_name()) &&
        StringUtils.isEmpty(request.getLast_name()) &&
        StringUtils.isEmpty(request.getBoss_k_pid()) &&
        StringUtils.isEmpty(request.getStruct_unit_id()) &&
        StringUtils.isEmpty(request.getUser_photo_link())
      ) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      if (request.getUser_id() == null) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      userIds.add(request.getUser_id());

      if (StringUtils.isNotEmpty(request.getStruct_unit_id())) {
        structUnitIds.add(UUID.fromString(request.getStruct_unit_id()));
      }

      validateFieldsLength(request);

      if (usernamesSet.contains(request.getUsername())) {
        throw new DuplicateValueInRequestException("Duplicate username in request", ObjectUtils.convertObjectToMap(request));
      }

      usernamesSet.add(request.getUsername());
    });

    Map<UUID, User> usersMap = findUsers(userIds);
    Map<UUID, StructUnit> structUnitsMap = findStructUnits(structUnitIds);

    try {
      return bulkUsersService.updateUsers(
        userRequests,
        usersMap,
        structUnitsMap,
        user
      );
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      throw new UsernameAlreadyExistsException(dataIntegrityViolationException);
    }
  }

  @Override
  public GetUsersResponse getUsersByParams (
    String name,
    SearchField searchField,
    SortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    boolean isSearchByAll = SearchField.ALL.equals(searchField);

    String username = SearchField.USERNAME.equals(searchField) || isSearchByAll ? name : null;
    String firstName = SearchField.FIRSTNAME.equals(searchField) || isSearchByAll ? name : null;
    String lastName = SearchField.LASTNAME.equals(searchField) || isSearchByAll ? name : null;
    String email = SearchField.EMAIL.equals(searchField) || isSearchByAll ? name : null;

    boolean isSearchByFullName = SearchField.FIRSTNAME_LASTNAME.equals(searchField) || SearchField.LASTNAME_FIRSTNAME.equals(searchField);

    if (StringUtils.isNotEmpty(name) &&isSearchByFullName) {
      String[] splittedString = name.split(" ");

      String lastWord = splittedString.length > 1 ? splittedString[1] : null;

      firstName = SearchField.FIRSTNAME_LASTNAME.equals(searchField) ? splittedString[0] : lastWord;
      lastName = SearchField.FIRSTNAME_LASTNAME.equals(searchField) ? lastWord : splittedString[0];
    }

    Page<UserWithLanguage> userList = userRepository.findAllByParamsPageable(
      name,
      username,
      firstName,
      lastName,
      email,
      isSearchByAll,
      PageRequest.of(pageNumber, pageSize, getSorting(sortField == null ? SortField.USERNAME : sortField, sortOrder))
    );

    List<GetUserResponse> userResponses = userList.stream().map(user -> new GetUserResponse(
      user.getUserId(),
      user.getUsername(),
      user.getEmail(),
      user.getFirstName(),
      user.getLastName(),
      user.getSource(),
      user.getLanguage(),
      user.getCreatedOn(),
      user.getCreatedBy(),
      user.getLastModifiedOn(),
      user.getLastModifiedBy(),
      user.getLastAuthTime()
    )).toList();

    return new GetUsersResponse(
      userList.getTotalElements(),
      pageSize,
      pageNumber,
      userResponses
    );
  }

  @Override
  public GetUserResponse getUserById (UUID userId) throws UserNotFoundException {
    Optional<UserWithLanguage> optionalUser = userRepository.findUserById(userId);

    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException();
    }

    UserWithLanguage user = optionalUser.get();

    return new GetUserResponse(
      user.getUserId(),
      user.getUsername(),
      user.getEmail(),
      user.getFirstName(),
      user.getLastName(),
      user.getSource(),
      user.getLanguage(),
      user.getCreatedOn(),
      user.getCreatedBy(),
      user.getLastModifiedOn(),
      user.getLastModifiedBy(),
      user.getLastAuthTime()
    );
  }

  @Override
  public GetUserRolesResponse getUserRoles (UUID userId) {
    List<UserRoleResponsibilityCount> responses = userRepository.findUserRoleResponsibilitiesCount(userId);

    List<GetUserRoleResponse> userRoles = responses.stream().map(role ->
      new GetUserRoleResponse(
        role.getRoleId(),
        role.getRoleName(),
        role.getRoleUsageCount()
      )
    ).toList();

    return new GetUserRolesResponse(
      (long) responses.size(),
      userRoles
    );
  }

  @Override
  @Transactional
  public void deleteUserById (UUID userId, User user) throws UserNotFoundException {
    User foundUser = findUserById(userId);

    userGroupsDAO.deleteAllByParams(userId, null, user);
    responsibilitiesDAO.deleteAllByParams(null, null, userId, null, null, null, user);
    globalResponsibilitiesDAO.deleteAllByUserId(userId, user);
    subscriptionsDAO.deleteAllByOwnerUserId(userId, user);

    foundUser.setIsDeleted(true);
    foundUser.setDeletedBy(user);
    foundUser.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    userRepository.save(foundUser);
    usersUtilsService.createUserHistory(user, MethodType.DELETE);

    roleActionCachingService.evictByValueInKey(userId.toString());
  }

  private Sort getSorting (SortField sortField, SortOrder sortOrder) {
    return switch (sortField) {
      case FIRSTNAME_LASTNAME -> Sort.by(
        SortUtils.getSortOrder(sortOrder, SortField.FIRSTNAME.getValue(), SortField.USERNAME.getValue()),
        SortUtils.getSortOrder(sortOrder, SortField.LASTNAME.getValue(), SortField.USERNAME.getValue())
      );
      case LASTNAME_FIRSTNAME -> Sort.by(
        SortUtils.getSortOrder(sortOrder, SortField.LASTNAME.getValue(), SortField.USERNAME.getValue()),
        SortUtils.getSortOrder(sortOrder, SortField.FIRSTNAME.getValue(), SortField.USERNAME.getValue())
      );
      default -> SortUtils.getSort(sortOrder, sortField.getValue(), SortField.USERNAME.getValue());
    };
  }

  private StructUnit findStructUnitById (String structUnitId) throws StructUnitNotFoundException {
    if (StringUtils.isEmpty(structUnitId)) return null;

    StructUnit structUnit = structUnitsDAO.findStructUnitByStructUnitId(UUID.fromString(structUnitId));
    if (structUnit == null) {
      throw new StructUnitNotFoundException();
    }

    return structUnit;
  }

  private Map<UUID, StructUnit> findStructUnits (Set<UUID> structUnitIds) throws StructUnitNotFoundException {
    List<StructUnit> structUnits = structUnitsDAO.findAllStructUnitsByIds(structUnitIds);

    Map<UUID, StructUnit> structUnitsMap = structUnits.stream().collect(Collectors.toMap(StructUnit::getStructUnitId, value -> value));

    if (structUnits.size() != structUnitIds.size()) {
      UUID absentStructUnitId = CollectionUtils.findFirstNotFoundValue(structUnitIds, structUnitsMap.keySet());

      PostOrPatchUserRequest request = new PostOrPatchUserRequest();
      request.setStruct_unit_id(absentStructUnitId.toString());

      throw new StructUnitNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return structUnitsMap;
  }

  private Map<UUID, User> findUsers (Set<UUID> userIds) throws UserNotFoundException {
    List<User> users = userRepository.findUsersByUserIds(userIds.stream().toList());

    Map<UUID, User> usersMap = users.stream().collect(Collectors.toMap(User::getUserId, value -> value));

    if (users.size() != userIds.size()) {
      UUID absentUserId = CollectionUtils.findFirstNotFoundValue(userIds, usersMap.keySet());

      PatchUserRequest request = new PatchUserRequest();
      request.setUser_id(absentUserId);

      throw new UserNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return usersMap;
  }

  private void validateFieldsLength (PostOrPatchUserRequest userRequest) throws InvalidFieldLengthException {
    if (
      StringUtils.isNotEmpty(userRequest.getUsername()) &&
      userRequest.getUsername().length() > 30
    ) {
      throw new InvalidFieldLengthException(ObjectUtils.convertObjectToMap(userRequest), "username", 30);
    }

    if (
      StringUtils.isNotEmpty(userRequest.getEmail()) &&
      userRequest.getEmail().length() > 60
    ) {
      throw new InvalidFieldLengthException(ObjectUtils.convertObjectToMap(userRequest), "email", 60);
    }

    if (
      StringUtils.isNotEmpty(userRequest.getFirst_name()) &&
      userRequest.getFirst_name().length() > 255
    ) {
      throw new InvalidFieldLengthException(ObjectUtils.convertObjectToMap(userRequest), "first_name", 255);
    }

    if (
      StringUtils.isNotEmpty(userRequest.getLast_name()) &&
      userRequest.getLast_name().length() > 255
    ) {
      throw new InvalidFieldLengthException(ObjectUtils.convertObjectToMap(userRequest), "last_name", 255);
    }
  }
}
