package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenInfoResponse {
  private Boolean active;

  private String uid;
}
