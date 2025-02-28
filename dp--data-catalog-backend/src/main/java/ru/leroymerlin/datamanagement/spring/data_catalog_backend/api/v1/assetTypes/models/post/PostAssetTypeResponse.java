package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class PostAssetTypeResponse implements Response {
  private UUID asset_type_id;

  private String asset_type_name;

  private String asset_type_description;

  private String source_language;

  private String asset_type_acronym;

  private String asset_type_color;

  private String asset_name_validation_mask;

  private String asset_name_validation_mask_example;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;
}
