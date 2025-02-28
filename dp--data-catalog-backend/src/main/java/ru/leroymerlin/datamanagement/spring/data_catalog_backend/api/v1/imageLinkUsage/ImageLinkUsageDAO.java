package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.imageLinkUsage;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.imageLinkUsage.ImageLinkUsageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.ImageLinkUsage;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;

/**
 * @author juliwolf
 */

@Service
public class ImageLinkUsageDAO {
  @Autowired
  private ImageLinkUsageRepository imageLinkUsageRepository;

  public ImageLinkUsage saveImageLinkUsage (ImageLinkUsage imageLinkUsage) {
    return imageLinkUsageRepository.save(imageLinkUsage);
  }

  public void deleteImagesLinkByAttributeId (List<UUID> attributeIds, User user) {
    imageLinkUsageRepository.deleteAllByAttributeId(attributeIds, user.getUserId());
  }
}
