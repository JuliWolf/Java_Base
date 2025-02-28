package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.GlobalResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityNotFoundException;

@Service
public class GlobalResponsibilitiesDAO {
  @Autowired
  protected GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  public GlobalResponsibility findGlobalResponsibilityById (UUID globalResponsibilityId) throws GlobalResponsibilityNotFoundException {
    Optional<GlobalResponsibility> globalResponsibility = globalResponsibilitiesRepository.findById(globalResponsibilityId);

    if (globalResponsibility.isEmpty()) {
      throw new GlobalResponsibilityNotFoundException(globalResponsibilityId);
    }

    if (globalResponsibility.get().getIsDeleted()) {
      throw new GlobalResponsibilityNotFoundException(globalResponsibilityId);
    }

    return globalResponsibility.get();
  }

  public GlobalResponsibility saveGlobalResponsibility (GlobalResponsibility globalResponsibility) {
    return globalResponsibilitiesRepository.save(globalResponsibility);
  }

  public void deleteAllByUserId (UUID userId, User user) {
    globalResponsibilitiesRepository.deleteByParams(null, userId, ResponsibleType.USER.toString(), user.getUserId());
  }

  public void deleteAllByRoleId (UUID roleId, User user) {
    globalResponsibilitiesRepository.deleteByParams(roleId, null, null, user.getUserId());
  }

  public void deleteAllByGroupId (UUID groupId, User user) {
    globalResponsibilitiesRepository.deleteByParams(null, groupId, ResponsibleType.GROUP.toString(), user.getUserId());
  }
}
