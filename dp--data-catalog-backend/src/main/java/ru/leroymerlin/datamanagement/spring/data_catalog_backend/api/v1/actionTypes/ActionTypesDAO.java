package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.ActionType;

/**
 * @author JuliWolf
 */
@Service
public class ActionTypesDAO {
  @Autowired
  private ActionTypeRepository actionTypeRepository;

  public ActionType findActionTypeById (UUID actionTypeId) throws ActionTypeNotFoundException {
    Optional<ActionType> actionType = actionTypeRepository.findById(actionTypeId);

    if (actionType.isEmpty()) {
      throw new ActionTypeNotFoundException();
    }

    if (actionType.get().getIsDeleted()) {
      throw new ActionTypeNotFoundException();
    }

    return actionType.get();
  }
}
