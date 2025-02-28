package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.CustomViewRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.CustomView;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class CustomViewsDAO {
  @Autowired
  protected CustomViewRepository customViewRepository;

  public CustomView findCustomViewById (UUID customViewId) throws CustomViewNotFoundException {
    Optional<CustomView> customView = customViewRepository.findById(customViewId);

    if (customView.isEmpty()) {
      throw new CustomViewNotFoundException();
    }

    if (customView.get().getIsDeleted()) {
      throw new CustomViewNotFoundException();
    }

    return customView.get();
  }

  public void deleteByParams (UUID assetTypeId, UUID roleId, User user) {
    customViewRepository.deleteByParams(assetTypeId, roleId, user.getUserId());
  }
}
