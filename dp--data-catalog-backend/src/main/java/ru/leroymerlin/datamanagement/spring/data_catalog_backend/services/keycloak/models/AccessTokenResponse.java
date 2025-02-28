package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models;

import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AccessTokenResponse {
  @JsonProperty("access_token")
  private String access_token;

  @JsonProperty("refresh_expires_in")
  private int refresh_expires_in;

  @JsonProperty("not-before-policy")
  private int not_before_policy;

  @JsonProperty("refresh_token")
  private String refresh_token;

  @JsonProperty("token_type")
  private String token_type;

  @JsonProperty("id_token")
  private String id_token;

  @JsonProperty("session_state")
  private String session_state;

  @JsonProperty("scope")
  private String scope;

  @JsonProperty("expires_in")
  private int expires_in;

  private Timestamp expiration_date;
}
