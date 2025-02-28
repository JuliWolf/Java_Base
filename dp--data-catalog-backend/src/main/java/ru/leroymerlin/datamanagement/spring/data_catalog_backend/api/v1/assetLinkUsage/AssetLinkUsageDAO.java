package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetLinkUsage;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetLinkUsage.AssetLinkUsageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetLinkUsage;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;

/**
 * @author juliwolf
 */

@Service
public class AssetLinkUsageDAO {
  @Autowired
  private AssetLinkUsageRepository assetLinkUsageRepository;

  public AssetLinkUsage saveAssetLinkUsage (AssetLinkUsage assetLinkUsage) {
    return assetLinkUsageRepository.save(assetLinkUsage);
  }

  public void deleteAssetLinkByAttributeId (List<UUID> attributeIds, User user) {
    assetLinkUsageRepository.deleteAllByAttributeId(
      attributeIds,
      user.getUserId()
    );
  }

  public void deleteAssetLinkByAssetIds (List<UUID> assetIds, User user) {
    assetLinkUsageRepository.deleteAssetLinkByAssetIds(
      assetIds,
      user.getUserId()
    );
  }
}
