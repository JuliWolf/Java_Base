package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions;

import java.util.UUID;

public class AttributeTypeIsUsedForAssetException extends RuntimeException {
  public AttributeTypeIsUsedForAssetException (UUID assetId) {
    super("This attribute type is still used for asset '" + assetId + "'");
  }
}
