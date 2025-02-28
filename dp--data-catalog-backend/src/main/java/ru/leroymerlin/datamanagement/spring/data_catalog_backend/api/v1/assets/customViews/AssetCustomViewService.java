package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews;

import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get.GetAssetCustomViewHeaderRows;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get.GetAssetCustomViewTableRows;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;

/**
 * @author juliwolf
 */

public interface AssetCustomViewService {
  GetAssetCustomViewHeaderRows getAssetCustomViewHeaderRows (UUID assetId, UUID customViewId) throws JsonProcessingException, AssetNotFoundException, CustomViewNotFoundException;

  GetAssetCustomViewTableRows getAssetCustomViewTableRows (
    UUID assetId,
    UUID customViewId,
    Integer pageNumber,
    Integer pageSize
  ) throws AssetNotFoundException, CustomViewNotFoundException;
}
