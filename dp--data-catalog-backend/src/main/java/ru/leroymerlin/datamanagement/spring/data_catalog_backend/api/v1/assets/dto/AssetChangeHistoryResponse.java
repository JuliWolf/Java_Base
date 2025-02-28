package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.dto;

import java.util.UUID;
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
public class AssetChangeHistoryResponse {
  private String entity_type_name;

  private String entity_type_name_ru;

  private String action_type_name;

  private String action_type_name_ru;

  private UUID asset_id;

  private java.sql.Timestamp logged_on;

  private UUID user_id;

  private String username;

  private String first_name;

  private String last_name;

  private UUID object_id;

  private UUID object_type_id;

  private String object_type_name;

  private String value;
}
