package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetHierarchy;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.AssetHierarchyRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.models.ParentChildAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetHierarchy;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;

/**
 * @author juliwolf
 */

@Service
public class AssetHierarchyDAO {
  @Autowired
  protected AssetHierarchyRepository assetHierarchyRepository;

  public void deleteAllByRelationId (List<UUID> relationIds, User user) {
    assetHierarchyRepository.deleteAllByRelationIds(relationIds, user.getUserId());
  }

  public AssetHierarchy saveAssetHierarchy (AssetHierarchy assetHierarchy) {
    return assetHierarchyRepository.save(assetHierarchy);
  }

  public Boolean isParentChildAssetsConnectionExists (UUID childAssetId, UUID parentAssetId) {
    return assetHierarchyRepository.isParentChildAssetsConnectionExists(childAssetId, parentAssetId);
  }

  public List<ParentChildAsset> findAllByChildParentAssets (List<UUID> childAssetIds, List<UUID> parentAssetIds) {
    return assetHierarchyRepository.findAllByChildParentAssets(childAssetIds, parentAssetIds);
  }
}
