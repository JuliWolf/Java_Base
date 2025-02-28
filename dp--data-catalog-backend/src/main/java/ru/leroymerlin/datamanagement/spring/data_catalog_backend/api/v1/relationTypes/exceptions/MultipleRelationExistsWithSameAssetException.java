package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class MultipleRelationExistsWithSameAssetException extends RuntimeException {
  public MultipleRelationExistsWithSameAssetException (UUID relationTypeComponentId) {
    super("Multiple relations with same asset for relation type component with id " + relationTypeComponentId + " already exist.");
  }
}
