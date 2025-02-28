package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoResponse {
  private String sub;

  private String uid;

  private Boolean email_verified;

  private String name;

  private String given_name;

  private String family_name;

  private String email;
}
