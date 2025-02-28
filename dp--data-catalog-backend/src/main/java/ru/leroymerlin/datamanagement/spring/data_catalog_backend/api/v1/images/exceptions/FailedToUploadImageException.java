package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.exceptions;

/**
 * @author juliwolf
 */

public class FailedToUploadImageException extends RuntimeException {
  public FailedToUploadImageException(String fileName) {
    super("Failed to upload filename:" + fileName);
  }
}
