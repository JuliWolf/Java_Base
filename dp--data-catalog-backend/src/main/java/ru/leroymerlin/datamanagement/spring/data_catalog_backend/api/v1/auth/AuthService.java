package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth;

import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.models.ProfileResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.GlobalResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.KeycloakService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.AccessTokenResponse;

/**
 * @author JuliWolf
 */
@Service
public class AuthService {
  private final KeycloakService keycloakService;

  private final UserRepository userRepository;

  private final LanguageService languageService;

  private final RolesDAO rolesDAO;

  private final GlobalResponsibilitiesDAO globalResponsibilitiesDAO;

  public AuthService (
    KeycloakService keycloakService,
    UserRepository userRepository,
    LanguageService languageService,
    RolesDAO rolesDAO,
    GlobalResponsibilitiesDAO globalResponsibilitiesDAO
  ) {
    this.keycloakService = keycloakService;
    this.userRepository = userRepository;
    this.languageService = languageService;
    this.rolesDAO = rolesDAO;
    this.globalResponsibilitiesDAO = globalResponsibilitiesDAO;
  }

  public String getKeycloakToken (String username, String password) throws UserNotFoundException {
    AccessTokenResponse token = keycloakService.checkUserAndGetToken(username, password);

    createOrUpdateUser(username, token);

    return token.getAccess_token();
  }

  public ProfileResponse getUserFromDb (String username) {
    Optional<User> userFromDb = userRepository.getUserByUsernameAndIsDeletedFalse(username);

    if (userFromDb.isEmpty()) {
      throw new UserNotFoundException();
    }

    User user = userFromDb.get();
    user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));

    user = userRepository.save(user);

    return new ProfileResponse(
      user.getUserId(),
      user.getUsername(),
      user.getEmail(),
      user.getFirstName(),
      user.getLastName(),
      user.getSource(),
      user.getLanguageName(),
      user.getCreatedOn(),
      user.getLastLoginTime()
    );
  }

  private void createOrUpdateUser (String username, AccessTokenResponse token) throws UserNotFoundException {
    Optional<User> userFromDb = userRepository.getUserByUsernameAndIsDeletedFalse(username);

    if (userFromDb.isEmpty()) {
      createUserFromKeycloak(token.getAccess_token());

      return;
    }

    User user = userFromDb.get();
    user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));

    userRepository.save(user);
  }

  public User createUserFromKeycloak (String accessToken) throws UserNotFoundException {
    // Create new user if user does not exist
    User user = keycloakService.getUserInfo(accessToken);

    Language ru = languageService.getLanguage("ru");
    user.setLanguage(ru);

    user = userRepository.save(user);

    createDefaultGlobalResponsibility(user);

    return user;
  }

  private void createDefaultGlobalResponsibility (User user) {
    try {
      Role role = rolesDAO.findRoleById(rolesDAO.DEFAULT_ROLE_ID);
      globalResponsibilitiesDAO.saveGlobalResponsibility(new GlobalResponsibility(
        user,
        null,
        role,
        ResponsibleType.USER,
        null
      ));
    } catch (RoleNotFoundException roleNotFoundException) {
      return;
    }
  }
}
