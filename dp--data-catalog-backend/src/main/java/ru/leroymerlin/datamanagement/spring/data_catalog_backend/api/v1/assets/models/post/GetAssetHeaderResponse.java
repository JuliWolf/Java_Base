package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetAssetHeaderResponse implements Response {
  private UUID asset_id;

  private String asset_name;

  private String asset_displayname;

  private UUID asset_type_id;

  private String asset_type_name;

  private UUID lifecycle_status;

  private String lifecycle_status_name;

  private UUID stewardship_status;

  private String stewardship_status_name;

  private String description;

  private List<GetAssetHeaderBusinessOwnerResponse> business_owners;

  private Boolean custom_views_flag;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;
}
