package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes.ActionTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes.ActionTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.ActionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author JuliWolf
 */
@Testable
public class ActionTypesDAOTest {
  @Autowired
  private ActionTypesDAO actionTypesDAO;

  @Autowired
  private ActionTypeRepository actionTypeRepository;

  @Test
  public void findEntityByIdNotFoundExceptionIntegrationTest () {
    try {
      assertThrows(ActionTypeNotFoundException.class, () -> actionTypesDAO.findActionTypeById(new UUID(123,123)));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void findEntityByIdSuccessIntegrationTest () {
    try {
      List<ActionType> actionTypes = actionTypeRepository.findAll();
      ActionType firstActionType = actionTypes.get(0);

      assertEquals(firstActionType.getActionTypeId().toString(), actionTypesDAO.findActionTypeById(firstActionType.getActionTypeId()).getActionTypeId().toString());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
