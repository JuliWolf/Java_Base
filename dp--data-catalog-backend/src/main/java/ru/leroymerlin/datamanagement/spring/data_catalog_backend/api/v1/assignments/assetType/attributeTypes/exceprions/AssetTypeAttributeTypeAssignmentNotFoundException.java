package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class AssetTypeAttributeTypeAssignmentNotFoundException extends RuntimeException {
  public AssetTypeAttributeTypeAssignmentNotFoundException () {
    super("asset type attribute type assignment not found");
  }

  public AssetTypeAttributeTypeAssignmentNotFoundException (UUID assetTypeAttributeTypeAssignmentId) {
    super("Asset type attribute type assignment with id " + assetTypeAttributeTypeAssignmentId + " not found");
  }
}
