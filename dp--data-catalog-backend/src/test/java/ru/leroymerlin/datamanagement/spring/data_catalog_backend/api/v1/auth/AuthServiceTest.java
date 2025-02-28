package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.AuthService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.LanguageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author JuliWolf
 */
@Testable
public class AuthServiceTest {

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected LanguageRepository languageRepository;

  @Autowired
  private AuthService authService;

  @AfterEach
  public void clearTables() {
    userRepository.deleteAll();
  }

  @Test
  public void getUserGetAndUpdateExistingUserIntegrationTest () {
    try {
      Optional<Language> ru = languageRepository.getLanguageByLanguage("ru");
      Language language;
      language = ru.orElseGet(() -> languageRepository.save(new Language("ru")));

      String username = "test_username";
      String firstName = "test_firstName";
      String lastName = "test_lastName";
      String email = "test@mail.com";

      User createdUser = new User(
          username,
          firstName,
          lastName,
          SourceType.KEYCLOAK,
          email
      );
      createdUser.setLanguage(language);
      createdUser.setLastLoginTime(null);
      createdUser = userRepository.save(createdUser);

      authService.getUserFromDb(username);

      Optional<User> updatedUser = userRepository.getUserByUsernameAndIsDeletedFalse(username);

      assertThat(updatedUser.get().getLastLoginTime()).isNotEqualTo(createdUser.getLastLoginTime());
    } catch (Exception ex) {
      System.out.println(ex);
      throw new RuntimeException();
    }
  }
}
