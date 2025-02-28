package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.models;

import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class TokenResponse implements Response {
  private String token;
}
