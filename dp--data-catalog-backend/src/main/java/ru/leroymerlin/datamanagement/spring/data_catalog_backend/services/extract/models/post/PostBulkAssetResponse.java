package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PostBulkAssetResponse {
  private UUID asset_id;

  private String asset_name;

  private String asset_displayname;

  private UUID asset_type_id;

  private String asset_type_name;

  private UUID lifecycle_status;

  private String lifecycle_status_name;

  private UUID stewardship_status;

  private String stewardship_status_name;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
