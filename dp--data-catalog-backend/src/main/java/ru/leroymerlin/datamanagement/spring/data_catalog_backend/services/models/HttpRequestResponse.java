package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HttpRequestResponse<T> {
  private int statusCode;

  private T responseBody;
}
