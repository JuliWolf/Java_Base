package ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.LanguageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
/**
 * @author JuliWolf
 */
@Testable
public class ServiceWithUserIntegrationTest {
  public final static String TEST_USERNAME = RandomStringUtils.random(30);

  @Autowired
  protected UserRepository userRepository;
  @Autowired
  protected LanguageRepository languageRepository;
  @Autowired
  private LogRepository logRepository;

  protected User user;
  protected Language language;

  @ClassRule
  public static PostgreSQLContainer<MockPostgreSQLContainer> postgreSQLContainer = MockPostgreSQLContainer.getInstance();

  @BeforeAll
  public void createUser () {
    Optional<Language> ru = languageRepository.getLanguageByLanguage("ru");

    if (ru.isEmpty()) {
      Language newLang = new Language("ru");
      language = languageRepository.save(newLang);
    } else {
      language = ru.get();
    }

    user = new User(TEST_USERNAME, "firstname", "secondname", SourceType.KEYCLOAK, "test@mail.com");
    user.setLanguage(language);
    user = userRepository.save(user);
  }

  @AfterAll
  public void clearUserAndLanguage () {
    logRepository.deleteAll();
    userRepository.deleteAll();
  }
}
