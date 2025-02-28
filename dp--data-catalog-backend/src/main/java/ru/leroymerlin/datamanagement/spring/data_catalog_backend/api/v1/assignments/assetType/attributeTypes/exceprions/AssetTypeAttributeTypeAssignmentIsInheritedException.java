package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions;

public class AssetTypeAttributeTypeAssignmentIsInheritedException extends RuntimeException {
  public AssetTypeAttributeTypeAssignmentIsInheritedException () {
    super("Asset type attribute type assignments is inherited");
  }
}
