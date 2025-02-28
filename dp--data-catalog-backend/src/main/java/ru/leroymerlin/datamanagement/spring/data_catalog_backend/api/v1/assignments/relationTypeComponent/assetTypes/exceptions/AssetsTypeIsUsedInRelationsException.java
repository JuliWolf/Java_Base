package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions;

/**
 * @author juliwolf
 */

public class AssetsTypeIsUsedInRelationsException extends RuntimeException {
  public AssetsTypeIsUsedInRelationsException () {
    super("This asset type is still used in some relations.");
  }
}
