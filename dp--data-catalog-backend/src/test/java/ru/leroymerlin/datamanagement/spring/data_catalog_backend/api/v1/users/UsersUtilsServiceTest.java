package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.UserHistory;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userHistory.UserHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author juliwolf
 */

@Testable
public class UsersUtilsServiceTest {
  @Autowired
  private UsersUtilsService usersUtilsService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserHistoryRepository userHistoryRepository;

  @AfterEach
  public void clearTables() {
    userHistoryRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  public void detectUserType_Success_Test () {
    assertAll(
      () -> assertEquals(UserType.SERVICE, usersUtilsService.detectUserType("60208754", "test@lemanapro.ru"), "service user"),
      () -> assertEquals(UserType.EXTERNAL, usersUtilsService.detectUserType("username1", null), "external user with empty email"),
      () -> assertEquals(UserType.INTERNAL, usersUtilsService.detectUserType("username1", "test@lemanapro.ru"), "internal user with @lemanapro.ru email"),
      () -> assertEquals(UserType.INTERNAL, usersUtilsService.detectUserType("username1", "test@lemanapro.kz"), "internal user with @lemanapro.kz email"),
      () -> assertEquals(UserType.EXTERNAL, usersUtilsService.detectUserType("username1", "test@google.com"), "external user with random email")
    );
  }

  @Test
  public void createUserHistory_Success_Test () {
    User createdUser = userRepository.save(new User("another username", "test@mail.com", "name", "second", SourceType.KEYCLOAK, null, UserType.SERVICE, UserWorkStatus.ACTIVE, null, null, null));

    usersUtilsService.createUserHistory(createdUser, MethodType.POST);

    List<UserHistory> postUsersHistories = userHistoryRepository.findAll();
    assertAll(
      () -> assertEquals(1, postUsersHistories.size(), "post createdUser history size"),
      () -> assertEquals(createdUser.getCreatedOn(), postUsersHistories.get(0).getValidFrom(), "post createdUser history valid from"),
      () -> assertEquals("3000-01-01 00:00:00.0", postUsersHistories.get(0).getValidTo().toString(), "post createdUser history valid to")
    );

    createdUser.setUsername("new username");
    createdUser.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    User updatedUser = userRepository.save(createdUser);
    usersUtilsService.createUserHistory(updatedUser, MethodType.PATCH);

    List<UserHistory> patchUsersHistories = userHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, patchUsersHistories.size(), "patch users history size"),
      () -> assertEquals(updatedUser.getLastModifiedOn(), patchUsersHistories.stream().filter(user -> user.getUsername().equals(updatedUser.getUsername())).findFirst().get().getValidFrom(), "patch createdUser history valid from"),
      () -> assertEquals(updatedUser.getLastModifiedOn(), patchUsersHistories.stream().filter(user -> user.getUsername().equals("another username")).findFirst().get().getValidTo(), "post createdUser history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", patchUsersHistories.stream().filter(user -> user.getUsername().equals(updatedUser.getUsername())).findFirst().get().getValidTo().toString(), "patched createdUser history valid to")
    );

    createdUser.setDeletedOn(new Timestamp(System.currentTimeMillis()));
    createdUser.setIsDeleted(true);
    User deletedUser = userRepository.save(createdUser);
    usersUtilsService.createUserHistory(createdUser, MethodType.DELETE);

    List<UserHistory> deleteUsersHistories = userHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(3, deleteUsersHistories.size(), "delete users history size"),
      () -> assertEquals(deletedUser.getDeletedOn(), deleteUsersHistories.stream().filter(UserHistory::getIsDeleted).findFirst().get().getValidFrom(), "deleted user history valid from"),
      () -> assertEquals(deletedUser.getDeletedOn(), deleteUsersHistories.stream().filter(user -> user.getUsername().equals(updatedUser.getUsername())).findFirst().get().getValidTo(), "patch user history valid to"),
      () -> assertEquals(deletedUser.getDeletedOn(), deleteUsersHistories.stream().filter(UserHistory::getIsDeleted).findFirst().get().getValidTo(), "deleted user history valid to")
    );
  }
}
