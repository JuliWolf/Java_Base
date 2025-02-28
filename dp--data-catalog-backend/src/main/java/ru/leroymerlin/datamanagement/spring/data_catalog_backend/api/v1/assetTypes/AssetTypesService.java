package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeHasChildAssetTypesException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypeChildrenResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PatchAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeResponse;

public interface AssetTypesService {
  PostAssetTypeResponse createAssetType(PostAssetTypeRequest assetTypeRequest, User user) throws IllegalArgumentException, AssetTypeNotFoundException;

  PostAssetTypeResponse updateAssetType (UUID assetTypeId, PatchAssetTypeRequest request, User user);

  GetAssetTypesResponse geAssetTypesByParams (Boolean rootFlag, String assetTypeName, String assetTypeDescription, Integer pageNumber, Integer pageSize);

  GetAssetTypeResponse getAssetTypeById (UUID assetTypeId) throws AssetTypeNotFoundException;

  void deleteAssetTypeById (UUID assetTypeId, User user) throws AssetTypeNotFoundException, AssetTypeHasChildAssetTypesException;

  GetAssetTypeChildrenResponse getAssetTypeChildren (UUID assetTypeId, Integer pageNumber, Integer pageSize) throws AssetTypeNotFoundException;
}
