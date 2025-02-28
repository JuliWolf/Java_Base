package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions;

/**
 * @author juliwolf
 */

public class RelationTypeComponentAssetTypeAssignmentIsInherited extends RuntimeException {
  public RelationTypeComponentAssetTypeAssignmentIsInherited () {
    super("Relation type component asset type is inherited");
  }
}
