package ru.leroymerlin.datamanagement.spring.data_catalog_backend.models;

import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements Response {
  protected String error;
}
