package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions;

public class AssetTypeStatusAssignmentIsInheritedException extends RuntimeException {
  public AssetTypeStatusAssignmentIsInheritedException () {
    super("Asset type status assignment is inherited");
  }
}
