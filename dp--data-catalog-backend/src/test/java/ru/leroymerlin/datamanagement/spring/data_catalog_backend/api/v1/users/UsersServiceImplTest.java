package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.UsernameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.structUnits.exceptions.StructUnitNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SearchField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserRoleResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostOrPatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.UserPhotoLink;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.SubscriptionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userHistory.UserHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
@Testable
public class UsersServiceImplTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UsersService usersService;

  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private ResponsibilityRepository responsibilityRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private UserHistoryRepository userHistoryRepository;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  User defaultUser;
  Asset firstAsset;
  Asset secondAsset;
  Asset thirdAsset;
  Role firstRole;
  Role secondRole;

  @BeforeAll
  public void prepareDefaultDta () {
    AssetType assetType = assetTypeRepository.save(new AssetType("test asset type", "desc", "acr", "color", null, null));
    firstAsset = assetRepository.save(new Asset("some asset", assetType, "asset desc", null, null, null, null));
    secondAsset = assetRepository.save(new Asset("another some asset", assetType, "asset desc", null, null, null, null));
    thirdAsset = assetRepository.save(new Asset("one more some asset", assetType, "asset desc", null, null, null, null));

    firstRole = roleRepository.save(new Role( "first role name", "some desc", null, null));
    secondRole = roleRepository.save(new Role( "second role name", "some desc", null, null));
  }

  @AfterAll
  public void clearDefaultData () {
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @BeforeEach
  public void createRole () {
    defaultUser = userRepository.save(new User("default", "default", "default", SourceType.API, "default@default.default"));
  }

  @AfterEach
  public void clearTables() {
    userHistoryRepository.deleteAll();
    subscriptionRepository.deleteAll();
    responsibilityRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  public void createUser_UserAlreadyExists_IntegrationTest () {
    PostOrPatchUserRequest request = new PostOrPatchUserRequest(defaultUser.getUsername(), defaultUser.getFirstName(), defaultUser.getLastName(), defaultUser.getEmail(), null, null, UserWorkStatus.ACTIVE, null);
    assertThrows(DataIntegrityViolationException.class, () -> usersService.createUser(request, defaultUser));
  }

  @Test
  public void createUser_StructUnitNotFoundException_IntegrationTest () {
    PostOrPatchUserRequest request = new PostOrPatchUserRequest("test user", "firstname", "lastname", "test@lemanapro.ru", null, UUID.randomUUID().toString(), UserWorkStatus.ACTIVE, null);

    assertThrows(StructUnitNotFoundException.class, () -> usersService.createUser(request, defaultUser));
  }

  @Test
  public void createUser_UserHistory_IntegrationTest () {
    PostOrPatchUserRequest request = new PostOrPatchUserRequest("test user", "firstname", "lastname", "test@lemanapro.ru", null, null, UserWorkStatus.ACTIVE, null);

    PostUserResponse response = usersService.createUser(request, defaultUser);

    List<UserHistory> userHistories = userHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(1, userHistories.size(), "user history size"),
      () -> assertEquals(response.getCreated_on(), userHistories.get(0).getValidFrom(), "post user history valid from"),
      () -> assertEquals("3000-01-01 00:00:00.0", userHistories.get(0).getValidTo().toString(), "post user history valid to")
    );
  }

  @Test
  public void createUser_Success_IntegrationTest () throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    UserPhotoLink userPhotoLink = new UserPhotoLink(List.of(new UserPhotoLink.Photo("s3", "https://storage.yandexcloud.net", "t-boss-pic-exch", "boss_pic/5.JPG", "boss")));
    PostOrPatchUserRequest request = new PostOrPatchUserRequest("test user", "firstname", "lastname", "test@lemanapro.ru", null, null, UserWorkStatus.ACTIVE, objectMapper.writeValueAsString(userPhotoLink));

    PostUserResponse response = usersService.createUser(request, defaultUser);

    assertAll(
      () -> assertEquals(defaultUser.getUserId(), response.getCreated_by()),
      () -> assertEquals(request.getUsername(), response.getUsername()),
      () -> assertEquals(SourceType.API, response.getSource()),
      () -> assertEquals(1, response.getUser_photo_link().getPhoto().size()),
      () -> assertEquals(userPhotoLink.getPhoto().get(0).getPhoto_source(), response.getUser_photo_link().getPhoto().get(0).getPhoto_source())
    );
  }

  @Test
  public void createUsersBulk_SomeRequiredFieldsAreEmptyException_IntegrationTest () {
    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest(null, "first name", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> usersService.createUsersBulk(requests, defaultUser));
  }

  @Test
  public void createUsersBulk_InvalidFieldLengthException_IntegrationTest () {
    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("first username", StringUtils.repeat("*", 256), "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(InvalidFieldLengthException.class, () -> usersService.createUsersBulk(requests, defaultUser));
  }

  @Test
  public void createUsersBulk_InvalidStructUnitId_IntegrationTest () {
    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("first username", "first name", "second name", "test@test.com", null, "123", UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(IllegalArgumentException.class, () -> usersService.createUsersBulk(requests, defaultUser));
  }

  @Test
  public void createUsersBulk_DuplicateValueInRequestException_IntegrationTest () {
    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("username", "first name", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(DuplicateValueInRequestException.class, () -> usersService.createUsersBulk(requests, defaultUser));
  }

  @Test
  public void createUsersBulk_StructUnitNotFoundException_IntegrationTest () {
    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("first username", "first name", "second name", "test@test.com", null, UUID.randomUUID().toString(), UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(StructUnitNotFoundException.class, () -> usersService.createUsersBulk(requests, defaultUser));
  }

  @Test
  public void createUsersBulk_UserHistory_IntegrationTest () {
    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("first username", "first name", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    requests.add(firstRequest);
    requests.add(secondRequest);

    List<PostUserResponse> response = usersService.createUsersBulk(requests, defaultUser);

    List<UserHistory> userHistories = userHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, userHistories.size(), "user history size"),
      () -> assertEquals(response.get(0).getCreated_on(), userHistories.stream().filter(user -> user.getUsername().equals(firstRequest.getUsername())).findFirst().get().getValidFrom(), "first post user history valid from"),
      () -> assertEquals(response.get(1).getCreated_on(), userHistories.stream().filter(user -> user.getUsername().equals(secondRequest.getUsername())).findFirst().get().getValidFrom(), "second post user history valid from"),
      () -> assertEquals("3000-01-01 00:00:00.0", userHistories.stream().filter(user -> user.getUsername().equals(firstRequest.getUsername())).findFirst().get().getValidTo().toString(), "first post user history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", userHistories.stream().filter(user -> user.getUsername().equals(secondRequest.getUsername())).findFirst().get().getValidTo().toString(), "first post user history valid to")
    );
  }

  @Test
  public void createUsersBulk_UsernameAlreadyExistsException_IntegrationTest () {
    User firstUser = userRepository.save(new User("first username", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));

    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("first username", "first name", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    requests.add(firstRequest);
    requests.add(secondRequest);

    assertThrows(UsernameAlreadyExistsException.class, () -> usersService.createUsersBulk(requests, defaultUser));
  }

  @Test
  public void createUsersBulk_Success_IntegrationTest () {
    List<PostOrPatchUserRequest> requests = new ArrayList<>();
    PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("first username", "first name", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
    requests.add(firstRequest);
    requests.add(secondRequest);

    List<PostUserResponse> responses = usersService.createUsersBulk(requests, defaultUser);

    assertAll(
      () -> assertEquals(firstRequest.getUsername(), responses.stream().filter(user -> user.getUsername().equals(firstRequest.getUsername())).findFirst().get().getUsername(), "first username"),
      () -> assertEquals(secondRequest.getUsername(), responses.stream().filter(user -> user.getUsername().equals(secondRequest.getUsername())).findFirst().get().getUsername(), "second username")
    );
  }

  @Test
  public void updateUser_UserNotFoundException_IntegrationTest () {
    PostOrPatchUserRequest request = new PostOrPatchUserRequest("test user", "firstname", "lastname", "test@lemanapro.ru", null, null, UserWorkStatus.ACTIVE, null);
    assertThrows(UserNotFoundException.class, () -> usersService.updateUser(UUID.randomUUID(), request, defaultUser));
  }

  @Test
  public void updateUser_InvalidFieldLengthException_IntegrationTest () {
    User firstUser = userRepository.save(new User("some user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));

    PostOrPatchUserRequest request = new PostOrPatchUserRequest(StringUtils.repeat("*", 31), "firstname", "lastname", "test@lemanapro.ru", null, null, UserWorkStatus.ACTIVE, null);
    assertThrows(InvalidFieldLengthException.class, () -> usersService.updateUser(firstUser.getUserId(), request, defaultUser));
  }

  @Test
  public void updateUser_StructUnitNotFoundException_IntegrationTest () {
    User firstUser = userRepository.save(new User("some user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));

    PostOrPatchUserRequest request = new PostOrPatchUserRequest("60208754", "firstname", "lastname", "test@lemanapro.ru", null, UUID.randomUUID().toString(), UserWorkStatus.ACTIVE, null);
    assertThrows(StructUnitNotFoundException.class, () -> usersService.updateUser(firstUser.getUserId(), request, defaultUser));
  }

  @Test
  public void updateUser_UserHistory_IntegrationTest () {
    PostOrPatchUserRequest postRequest = new PostOrPatchUserRequest("test user", "firstname", "lastname", "test@lemanapro.ru", null, null, UserWorkStatus.ACTIVE, null);

    PostUserResponse postResponse = usersService.createUser(postRequest, defaultUser);

    PostOrPatchUserRequest patchRequest = new PostOrPatchUserRequest("60208754", null, null, null, null, null, null, null);
    PatchUserResponse patchResponse = usersService.updateUser(postResponse.getUser_id(), patchRequest, defaultUser);

    List<UserHistory> userHistories = userHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, userHistories.size(), "users history size"),
      () -> assertEquals(postResponse.getCreated_on(), userHistories.stream().filter(user -> user.getUsername().equals(postResponse.getUsername())).findFirst().get().getValidFrom(), "post user history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), userHistories.stream().filter(user -> user.getUsername().equals(patchResponse.getUsername())).findFirst().get().getValidFrom(), "patch user history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), userHistories.stream().filter(user -> user.getUsername().equals(postResponse.getUsername())).findFirst().get().getValidTo(), "post user history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", userHistories.stream().filter(user -> user.getUsername().equals(patchResponse.getUsername())).findFirst().get().getValidTo().toString(), "patched user history valid to")
    );
  }

  @Test
  public void updateUser_UserAlreadyExists_IntegrationTest () {
    User firstUser = userRepository.save(new User("some user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));
    User secondUser = userRepository.save(new User("60208754", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));

    PostOrPatchUserRequest request = new PostOrPatchUserRequest("60208754", null, "new last name", "test@lemanapro.kz", null, null, null, null);
    assertThrows(DataIntegrityViolationException.class, () -> usersService.updateUser(firstUser.getUserId(), request, defaultUser));
  }

  @Test
  public void updateUser_Success_IntegrationTest () {
    User firstUser = userRepository.save(new User("some user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));
    PostOrPatchUserRequest request = new PostOrPatchUserRequest("60208754", null, "new last name", "test@lemanapro.kz", null, null, null, null);

    PatchUserResponse user = usersService.updateUser(firstUser.getUserId(), request, defaultUser);

    assertAll(
      () -> assertEquals(firstUser.getFirstName(), user.getFirst_name(), "user first name"),
      () -> assertEquals(request.getUsername(), user.getUsername(), "user username"),
      () -> assertEquals(UserType.SERVICE, user.getUser_type(), "user service")
    );
  }

  @Test
  public void getUsersByParams_Pagination_IntegrationTest () {
    generateUsers(130);

    assertAll(
      () -> assertEquals(1, usersService.getUsersByParams("110", SearchField.USERNAME, null, null, 0, 50).getResults().size()),
      () -> assertEquals(100, usersService.getUsersByParams(null, null, null, null, 0, 150).getResults().size()),
      () -> assertEquals(0, usersService.getUsersByParams(null, null, null, null, 10, 50).getResults().size()),
      () -> assertEquals(131, usersService.getUsersByParams(null, null, null, null, 0, 50).getTotal()),
      () -> assertEquals(11, usersService.getUsersByParams("11", SearchField.LASTNAME, null, null, 2, 50).getTotal())
    );
  }

  @Test
  public void getUsersByParams_Success_IntegrationTest () {
    User firstUser = userRepository.save(new User("some user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));
    User secondUser = userRepository.save(new User("test number 2", "has not name", "and last name no", SourceType.API, "noname@mail.com"));
    User thirdUser = userRepository.save(new User("just John", "Johan", "Rastrovich", SourceType.API, "rastrovich@google.com"));
    User forthUser = userRepository.save(new User("nickname", "Cool", "Boy", SourceType.KEYCLOAK, "coolBoy@yooho.ru"));

    assertAll(
      () -> assertEquals(5,  usersService.getUsersByParams(null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(5,  usersService.getUsersByParams("nickname", null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0,  usersService.getUsersByParams("nickname", SearchField.LASTNAME, null, null, 0, 50).getResults().size()),
      () -> assertEquals(1,  usersService.getUsersByParams("nickname", SearchField.USERNAME, null, null, 0, 50).getResults().size()),
      () -> assertEquals(2,  usersService.getUsersByParams("o", SearchField.USERNAME, null, null, 0, 50).getResults().size()),
      () -> assertEquals(4,  usersService.getUsersByParams("o", SearchField.ALL, null, null, 0, 50).getResults().size()),
      () -> assertEquals(3,  usersService.getUsersByParams("com", SearchField.ALL, null, null, 0, 50).getResults().size()),
      () -> assertEquals(2,  usersService.getUsersByParams("name", SearchField.LASTNAME, null, null, 0, 50).getResults().size()),
      () -> assertEquals(2,  usersService.getUsersByParams("name name", SearchField.FIRSTNAME_LASTNAME, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0,  usersService.getUsersByParams("Rastrovich Johan", SearchField.FIRSTNAME_LASTNAME, null, null, 0, 50).getResults().size()),
      () -> assertEquals(1,  usersService.getUsersByParams("Boy Cool", SearchField.LASTNAME_FIRSTNAME, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0,  usersService.getUsersByParams("Cool Boy", SearchField.LASTNAME_FIRSTNAME, null, null, 0, 50).getResults().size())
    );
  }

  @Test
  public void getUserById_UserNotFoundException_IntegrationTest () {
    assertThrows(UserNotFoundException.class, () -> usersService.getUserById(UUID.randomUUID()));
  }

  @Test
  public void getUserById_Success_IntegrationTest () {
    User user = userRepository.save(new User("some user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));

    GetUserResponse response = usersService.getUserById(user.getUserId());

    assertAll(
      () -> assertEquals(user.getUsername(), response.getUsername()),
      () -> assertEquals(user.getSource(), response.getSource())
    );
  }

  @Test
  public void getUserRoles_Success_IntegrationTest () {
    User user = userRepository.save(new User("some user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com"));
    Responsibility firstResponsibility = responsibilityRepository.save(new Responsibility(user, null, firstAsset, firstRole, ResponsibleType.USER, defaultUser));
    Responsibility secondResponsibility = responsibilityRepository.save(new Responsibility(user, null, secondAsset, secondRole, ResponsibleType.USER, defaultUser));
    Responsibility thirdResponsibility = responsibilityRepository.save(new Responsibility(user, null, thirdAsset, secondRole, ResponsibleType.USER, defaultUser));

    GetUserRolesResponse userRoles = usersService.getUserRoles(user.getUserId());
    GetUserRoleResponse firstUserRole = userRoles.getResults().stream().filter(role -> role.getRole_id().equals(firstRole.getRoleId())).findFirst().get();
    GetUserRoleResponse secondUserRole = userRoles.getResults().stream().filter(role -> role.getRole_id().equals(secondRole.getRoleId())).findFirst().get();

    assertAll(
      () -> assertEquals(2, userRoles.getTotal()),
      () -> assertEquals(firstRole.getRoleName(), firstUserRole.getRole_name()),
      () -> assertEquals(1, firstUserRole.getCount()),
      () -> assertEquals(secondRole.getRoleName(), secondUserRole.getRole_name()),
      () -> assertEquals(2, secondUserRole.getCount())
    );
  }

  @Test
  public void deleteUserById_UserAlreadyDeleted_IntegrationTest () {
    User user = new User("test user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com");
    user.setIsDeleted(true);
    userRepository.save(user);

    assertThrows(UserNotFoundException.class, () -> usersService.deleteUserById(user.getUserId(), user));
  }

  @Test
  public void deleteUserById_DeleteConnectedGlobalResponsibilities_IntegrationTest () {
    User user = new User("test user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com");
    userRepository.save(user);

    GlobalResponsibility globalResponsibility = globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, null, ResponsibleType.USER, user));

    usersService.deleteUserById(user.getUserId(), user);

    Optional<GlobalResponsibility> deletedGlobalResponsibility = globalResponsibilitiesRepository.findById(globalResponsibility.getGlobalResponsibilityId());
    assertAll(
      () -> assertTrue(deletedGlobalResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedGlobalResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedGlobalResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteUserById_DeleteConnectedResponsibilities_IntegrationTest () {
    User user = new User("test user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com");
    userRepository.save(user);

    Role role = roleRepository.save(new Role( "role name", "some desc", null, defaultUser));
    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, null, role, ResponsibleType.USER, defaultUser));
    usersService.deleteUserById(user.getUserId(), defaultUser);

    Optional<Responsibility> deletedResponsibility = responsibilityRepository.findById(responsibility.getResponsibilityId());
    assertAll(
      () -> assertTrue(deletedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedResponsibility.get().getDeletedOn()),
      () -> assertEquals(defaultUser.getUserId(), deletedResponsibility.get().getDeletedBy().getUserId())
    );

    responsibilityRepository.delete(responsibility);
    roleRepository.delete(role);
  }

  @Test
  public void deleteUserById_DeleteConnectedSubscriptions_IntegrationTest () {
    User user = new User("test user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com");
    userRepository.save(user);

    Subscription subscription = subscriptionRepository.save(new Subscription(user, firstAsset, "* */5 * * * *", user));
    usersService.deleteUserById(user.getUserId(), defaultUser);

    Optional<Subscription> deletedSubscription = subscriptionRepository.findById(subscription.getSubscriptionId());
    assertAll(
      () -> assertTrue(deletedSubscription.get().getIsDeleted()),
      () -> assertNotNull(deletedSubscription.get().getDeletedOn()),
      () -> assertEquals(defaultUser.getUserId(), deletedSubscription.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteUserById_Success_IntegrationTest () {
    User user = new User("test user", "firstname", "lastname", SourceType.KEYCLOAK, "test@mail.com");
    userRepository.save(user);

    usersService.deleteUserById(user.getUserId(), user);
    Optional<User> foundUser = userRepository.findById(user.getUserId());

    assertAll(
      () -> assertTrue(foundUser.get().getIsDeleted()),
      () -> assertEquals(foundUser.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  private void generateUsers (int count) {
    for (int i = 0; i < count; i++) {
      userRepository.save(new User("user_"+i, "firstname_"+i, "lastname_"+i, SourceType.KEYCLOAK, "test@mail.com"));
    }
  }
}
