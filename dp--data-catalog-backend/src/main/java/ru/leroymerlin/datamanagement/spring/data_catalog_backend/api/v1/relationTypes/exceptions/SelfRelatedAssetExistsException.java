package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

/**
 * @author JuliWolf
 */
public class SelfRelatedAssetExistsException extends RuntimeException {
  public SelfRelatedAssetExistsException () {
    super("Self related assets still exist for this relation type.");
  }
}
