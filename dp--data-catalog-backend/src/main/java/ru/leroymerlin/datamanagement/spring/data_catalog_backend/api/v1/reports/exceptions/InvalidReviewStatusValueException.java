package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions;

/**
 * @author juliwolf
 */

public class InvalidReviewStatusValueException extends RuntimeException {
  public InvalidReviewStatusValueException () {
    super("Invalid review status value");
  }
}
