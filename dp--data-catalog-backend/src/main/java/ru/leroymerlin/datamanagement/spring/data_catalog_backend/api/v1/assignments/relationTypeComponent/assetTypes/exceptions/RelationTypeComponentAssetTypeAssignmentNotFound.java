package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions;

import java.util.UUID;

/**
 * @author juliwolf
 */

public class RelationTypeComponentAssetTypeAssignmentNotFound extends RuntimeException {
  public RelationTypeComponentAssetTypeAssignmentNotFound () {
    super("relation type component asset type assignment not found");
  }

  public RelationTypeComponentAssetTypeAssignmentNotFound (UUID relationTypeComponentAssetTypeAssignmentId) {
    super("Relation type component asset type assignment with id "+ relationTypeComponentAssetTypeAssignmentId + " not found");
  }
}
