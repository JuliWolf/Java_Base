package ru.leroymerlin.datamanagement.spring.data_catalog_backend.models;

import lombok.*;

/**
 * @author juliwolf
 */

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ErrorWithDetailsResponse<T> extends ErrorResponse {
  private T details;

  public ErrorWithDetailsResponse (String error, T details) {
    super(error);

    this.details = details;
  }

  public ErrorWithDetailsResponse (String error) {
    super(error);
  }
}
