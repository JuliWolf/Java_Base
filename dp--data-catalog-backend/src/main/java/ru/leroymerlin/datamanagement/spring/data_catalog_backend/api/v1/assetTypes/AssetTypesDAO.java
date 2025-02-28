package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;

@Service
public class AssetTypesDAO {
  protected final AssetTypeRepository assetTypeRepository;

  public AssetTypesDAO (AssetTypeRepository assetTypeRepository) {
    this.assetTypeRepository = assetTypeRepository;
  }

  public AssetType findAssetTypeById(UUID assetTypeId) throws AssetTypeNotFoundException {
    Optional<AssetType> assetType = assetTypeRepository.findById(assetTypeId);

    if (assetType.isEmpty()) {
      throw new AssetTypeNotFoundException();
    }

    if (assetType.get().getIsDeleted()) {
      throw new AssetTypeNotFoundException();
    }

    return assetType.get();
  }

  public boolean isAssetTypeExists (UUID assetTypeId) {
    return assetTypeRepository.existsByAssetTypeIdAndIsDeletedFalse(assetTypeId);
  }

  public List<AssetType> findAllAssetTypesByParentAssetTypeId (UUID parentAssetTypeId) {
    return assetTypeRepository.findAllAssetTypesByParentAssetTypeId(parentAssetTypeId);
  }

  public List<AssetType> findAssetTypeByIds (List<UUID> assetTypeIds) {
    return assetTypeRepository.findAssetTypeByIds(assetTypeIds);
  }
}
