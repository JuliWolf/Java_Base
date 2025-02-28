package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions;

/**
 * @author juliwolf
 */

public class AssetNameValidationMaskDoesNotMatchExampleException extends RuntimeException {
  public AssetNameValidationMaskDoesNotMatchExampleException () {
    super("asset_name_validation_mask_example does not match asset_name_validation_mask");
  }
}
