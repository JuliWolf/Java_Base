package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostOrPatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.StructUnit;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;

/**
 * @author juliwolf
 */

@Service
public class BulkUsersService {
  private final LanguageService languageService;

  private final UsersDAO usersDAO;

  private final UsersUtilsService usersUtilsService;

  public BulkUsersService (
    LanguageService languageService,
    UsersDAO usersDAO,
    UsersUtilsService usersUtilsService
  ) {
    this.languageService = languageService;
    this.usersDAO = usersDAO;
    this.usersUtilsService = usersUtilsService;
  }

  @Transactional
  public List<PostUserResponse> createUsers (
    List<PostOrPatchUserRequest> userRequests,
    Map<UUID, StructUnit> structUnitsMap,
    User user
  ) {
    Language ru = languageService.getLanguage("ru");

    return userRequests.stream().map(request -> {
      StructUnit structUnit = StringUtils.isNotEmpty(request.getStruct_unit_id())
        ? structUnitsMap.get(UUID.fromString(request.getStruct_unit_id()))
        : null;

      UserType userType = usersUtilsService.detectUserType(request.getUsername(), request.getEmail());

      User createdUser = usersDAO.userRepository.save(new User(
        request.getUsername(),
        request.getEmail(),
        request.getFirst_name(),
        request.getLast_name(),
        SourceType.API,
        structUnit,
        userType,
        request.getUser_work_status() == null ? UserWorkStatus.ACTIVE : request.getUser_work_status(),
        request.getUser_photo_link(),
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
        usersUtilsService.parsePhotoLinks(request.getUser_photo_link()),
        ru.getLanguage(),
        createdUser.getCreatedOn(),
        user.getUserId()
      );
    }).toList();
  }

  @Transactional
  public List<PatchUserResponse> updateUsers (
    List<PatchUserRequest> userRequests,
    Map<UUID, User> usersMap,
    Map<UUID, StructUnit> structUnitsMap,
    User user
  ) {
    return userRequests.stream().map(request -> {
      User foundUser = usersMap.get(request.getUser_id());

      StructUnit structUnit = StringUtils.isNotEmpty(request.getStruct_unit_id())
        ? structUnitsMap.get(UUID.fromString(request.getStruct_unit_id()))
        : foundUser.getStructUnit();

      return updateUser(
        request,
        foundUser,
        structUnit,
        user
      );
    }).toList();
  }

  @Transactional
  public PatchUserResponse updateUser (
    PostOrPatchUserRequest request,
    User foundUser,
    StructUnit structUnit,
    User user
  ) {
    if (StringUtils.isNotEmpty(request.getStruct_unit_id())) {
      foundUser.setStructUnit(structUnit);
    }

    if (StringUtils.isNotEmpty(request.getUsername())) {
      foundUser.setUsername(request.getUsername());
    }

    if (StringUtils.isNotEmpty(request.getFirst_name())) {
      foundUser.setFirstName(request.getFirst_name());
    }

    if (StringUtils.isNotEmpty(request.getLast_name())) {
      foundUser.setLastName(request.getLast_name());
    }

    if (StringUtils.isNotEmpty(request.getEmail())) {
      foundUser.setEmail(request.getEmail());
    }

    if (StringUtils.isNotEmpty(request.getBoss_k_pid())) {
      foundUser.setBossKPid(request.getBoss_k_pid());
    }

    if (StringUtils.isNotEmpty(request.getUsername()) || StringUtils.isNotEmpty(request.getEmail())) {
      UserType userType = usersUtilsService.detectUserType(foundUser.getUsername(), foundUser.getEmail());
      foundUser.setUserType(userType);
    }

    if (request.getUser_work_status() != null) {
      foundUser.setUserWorkStatus(request.getUser_work_status());
    }

    foundUser.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    foundUser.setModifiedBy(user);

    User updatedUser = usersDAO.userRepository.save(foundUser);

    usersUtilsService.createUserHistory(updatedUser, MethodType.PATCH);

    return new PatchUserResponse(
      updatedUser.getUserId(),
      updatedUser.getUsername(),
      updatedUser.getEmail(),
      updatedUser.getFirstName(),
      updatedUser.getLastName(),
      updatedUser.getSource(),
      updatedUser.getBossKPid(),
      updatedUser.getStructUnit() != null ? updatedUser.getStructUnit().getStructUnitId() : null,
      updatedUser.getUserType(),
      updatedUser.getUserWorkStatus(),
      usersUtilsService.parsePhotoLinks(request.getUser_photo_link()),
      updatedUser.getLanguageName(),
      updatedUser.getCreatedOn(),
      updatedUser.getCreatedByUUID(),
      updatedUser.getLastModifiedOn(),
      updatedUser.getLastModifiedByUUID()
    );
  }
}
