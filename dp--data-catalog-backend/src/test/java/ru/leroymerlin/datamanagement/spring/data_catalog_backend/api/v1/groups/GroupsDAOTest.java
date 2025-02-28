package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups.GroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Group;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testable
public class GroupsDAOTest {
  @Autowired
  private GroupsDAO groupsDAO;

  @Autowired
  private GroupRepository groupRepository;

  @Test
  public void findGroupByIdNotFoundExceptionIntegrationTest () {
    try {
      assertThrows(GroupNotFoundException.class, () -> groupsDAO.findGroupById(new UUID(123,123)));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void findGroupByIdSuccessIntegrationTest () {
    try {
      Group firstGroup = groupRepository.save(new Group("test_name", "description", "email", "loop", null));

      assertEquals(firstGroup.getGroupId().toString(), groupsDAO.findGroupById(firstGroup.getGroupId()).getGroupId().toString());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
