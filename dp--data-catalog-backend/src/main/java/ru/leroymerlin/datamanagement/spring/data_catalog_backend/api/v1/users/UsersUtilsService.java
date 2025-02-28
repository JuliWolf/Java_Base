package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.UserPhotoLink;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.MethodType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.GlobalResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.UserHistory;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userHistory.UserHistoryRepository;

/**
 * @author juliwolf
 */

@Service
public class UsersUtilsService {
  private final String SERVICE_USERNAME = "^[3,6]+[0-9]{7}$";

  private final String[] INTERNAL_EMAILS = new String[]{ "@lemanapro.ru", "@lemanapro.kz" };

  private final RolesDAO rolesDAO;

  protected final GlobalResponsibilitiesDAO globalResponsibilitiesDAO;

  private final UserHistoryRepository userHistoryRepository;

  public UsersUtilsService (
    RolesDAO rolesDAO,
    GlobalResponsibilitiesDAO globalResponsibilitiesDAO,
    UserHistoryRepository userHistoryRepository
  ) {
    this.rolesDAO = rolesDAO;
    this.globalResponsibilitiesDAO = globalResponsibilitiesDAO;
    this.userHistoryRepository = userHistoryRepository;
  }

  public UserPhotoLink parsePhotoLinks (String photoLinks) {
    if (StringUtils.isEmpty(photoLinks)) return null;

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(photoLinks, UserPhotoLink.class);
    } catch (JsonProcessingException jsonProcessingException) {
      return null;
    }
  }

  public UserType detectUserType (String userName, String email) {
    Matcher usernameMatcher = Pattern.compile(SERVICE_USERNAME, Pattern.CASE_INSENSITIVE).matcher(userName.toLowerCase());

    if (usernameMatcher.find()) {
      return UserType.SERVICE;
    }

    if (StringUtils.isEmpty(email)) {
      return UserType.EXTERNAL;
    }

    if (Arrays.stream(INTERNAL_EMAILS).anyMatch(email::contains)) {
      return UserType.INTERNAL;
    }

    return UserType.EXTERNAL;
  }

  @Transactional
  public void createUserHistory (User user, MethodType methodType) {
    UserHistory userHistory = new UserHistory(user);

    switch (methodType) {
      case POST -> userHistory.setCreatedValidDate();
      case PATCH -> {
        userHistory.setUpdatedValidDate();
        userHistoryRepository.updateLastUserHistory(
          user.getLastModifiedOn(),
          user.getUserId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
      case DELETE -> {
        userHistory.setDeletedValidDate();

        userHistoryRepository.updateLastUserHistory(
          user.getDeletedOn(),
          user.getUserId(),
          HistoryDateUtils.getValidToDefaultTime()
        );
      }
    }

    userHistoryRepository.save(userHistory);
  }

  public void createDefaultGlobalResponsibility (User createdUser, User user) {
    try {
      Role role = rolesDAO.findRoleById(rolesDAO.DEFAULT_ROLE_ID);
      globalResponsibilitiesDAO.saveGlobalResponsibility(new GlobalResponsibility(
        createdUser,
        null,
        role,
        ResponsibleType.USER,
        user
      ));
    } catch (RoleNotFoundException ignored) {}
  }
}
