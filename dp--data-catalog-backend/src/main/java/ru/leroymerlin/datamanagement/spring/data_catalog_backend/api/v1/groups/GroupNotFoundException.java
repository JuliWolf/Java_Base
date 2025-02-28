package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class GroupNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public GroupNotFoundException() {
    super("group not found");
  }

  public GroupNotFoundException(UUID groupId) {
    super("Group with id " + groupId + " not found");
  }

  public GroupNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
