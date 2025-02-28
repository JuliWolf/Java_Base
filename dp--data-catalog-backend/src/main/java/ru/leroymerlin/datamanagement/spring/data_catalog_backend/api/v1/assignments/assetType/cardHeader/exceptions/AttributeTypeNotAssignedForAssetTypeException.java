package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.exceptions;

/**
 * @author juliwolf
 */

public class AttributeTypeNotAssignedForAssetTypeException extends RuntimeException {
  public AttributeTypeNotAssignedForAssetTypeException () {
    super("This attribute type isn’t assigned to this asset type.");
  }
}
