package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class MultipleRelationExistsWithAssetException extends RuntimeException {
  public MultipleRelationExistsWithAssetException (UUID relationTypeId) {
    super("Same assets relations still exist for this " + relationTypeId + ".");
  }
}
