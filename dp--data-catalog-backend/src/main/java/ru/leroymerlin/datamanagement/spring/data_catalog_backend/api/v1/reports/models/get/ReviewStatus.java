package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get;

import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.InvalidReviewStatusValueException;

/**
 * @author juliwolf
 */

@Getter
public enum ReviewStatus {
  REVIEW_NOT_DONE("Не пройдено ревью"),
  REVIEW_NOT_REQUIRED("Не требует ревью"),
  REVIEW_NOT_REQUESTED("Не подан на ревью"),
  REVIEW_DONE("Пройдено ревью");

  private String value;

  ReviewStatus (String value) {
    this.value = value;
  }

  public static ReviewStatus getReviewStatus (String value) {
    for (ReviewStatus status : ReviewStatus.values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }

    throw new InvalidReviewStatusValueException();
  }
}
