package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions;

public class AssetTypeHasChildAssetTypesException extends RuntimeException {
  public AssetTypeHasChildAssetTypesException () {
    super("There are hidden asset types for this asset type");
  }
}
