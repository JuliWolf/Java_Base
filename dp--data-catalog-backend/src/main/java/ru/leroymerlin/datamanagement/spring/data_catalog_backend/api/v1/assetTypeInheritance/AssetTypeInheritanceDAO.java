package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypeInheritance;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypeInheritance.AssetTypeInheritanceRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeInheritance;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;

@Service
public class AssetTypeInheritanceDAO {
  @Autowired
  private AssetTypeInheritanceRepository assetTypeInheritanceRepository;

  public AssetTypeInheritance saveAssetTypeInheritance (AssetTypeInheritance assetTypeInheritance) {
    return assetTypeInheritanceRepository.save(assetTypeInheritance);
  }

  public Boolean isAssetTypeInheritanceExistsByParentAndChildAssetType (UUID parentAssetTypeId, UUID childAssetTypeId) {
    return assetTypeInheritanceRepository.isAssetTypeInheritanceExistsByParentAssetTypeIdAndChildAssetTypeId(parentAssetTypeId, childAssetTypeId);
  }

  public void deleteAllByChildAssetTypeId (UUID childAssetTypeId, User user) {
    assetTypeInheritanceRepository.deleteByChildAssetTypeId(childAssetTypeId, user.getUserId());
  }
}
