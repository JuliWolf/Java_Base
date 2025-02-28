package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAssetTypeResponse implements Response {
  private UUID asset_type_id;

  private String asset_type_name;

  private String asset_type_description;

  private String source_language;

  private String asset_type_acronym;

  private String asset_type_color;

  private String asset_name_validation_mask;

  private String asset_name_validation_mask_example;

  private Boolean root_flag;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;
}
