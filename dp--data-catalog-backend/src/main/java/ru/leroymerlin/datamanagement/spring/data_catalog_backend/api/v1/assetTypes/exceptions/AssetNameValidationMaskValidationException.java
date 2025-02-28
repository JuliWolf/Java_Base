package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions;

/**
 * @author juliwolf
 */

public class AssetNameValidationMaskValidationException extends RuntimeException {
  public AssetNameValidationMaskValidationException () {
    super("asset_name_validation_mask_example and asset_name_validation_mask should be filled both");
  }
}
