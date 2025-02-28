package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class UserNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public UserNotFoundException() {
    super("user not found");
  }

  public UserNotFoundException(UUID userId) {
    super("User with id "+ userId + " not found");
  }

  public UserNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
