package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Testable
public class UsersDAOTest {
  @Autowired
  private UsersDAO usersDAO;

  @Test
  public void findUserByIdNotFoundIntegrationTest () {
    assertThrows(UserNotFoundException.class, () -> usersDAO.findUserById(new UUID(123, 123)));
  }
}
