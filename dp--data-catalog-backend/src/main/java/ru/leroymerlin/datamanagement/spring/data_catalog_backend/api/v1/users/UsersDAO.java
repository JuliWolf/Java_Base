package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;

@Service
public class UsersDAO {
  protected final UserRepository userRepository;

  protected final GlobalResponsibilitiesDAO globalResponsibilitiesDAO;

  public UsersDAO (
    UserRepository userRepository,
    GlobalResponsibilitiesDAO globalResponsibilitiesDAO
  ) {
    this.userRepository = userRepository;
    this.globalResponsibilitiesDAO = globalResponsibilitiesDAO;
  }

  public User findUserById (UUID userId) throws UserNotFoundException {
    Optional<User> user = userRepository.findById(userId);

    if (!isUserActive(user)) {
      throw new UserNotFoundException(userId);
    }

    return user.get();
  }

  private Boolean isUserActive (Optional<User> user) {
    return user.filter(value -> !value.getIsDeleted()).isPresent();
  }

  public Optional<User> getUserByUsernameAndIsDeletedFalse (String username) {
    return userRepository.getUserByUsernameAndIsDeletedFalse(username);
  }

  public List<User> findUsersByUserIds(List<UUID> userIds) {
    return userRepository.findUsersByUserIds(userIds);
  }
}
