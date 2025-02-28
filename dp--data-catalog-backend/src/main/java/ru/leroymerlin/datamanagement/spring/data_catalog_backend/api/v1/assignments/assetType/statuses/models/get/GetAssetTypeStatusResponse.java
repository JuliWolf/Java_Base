package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetAssetTypeStatusResponse {
  private UUID asset_type_status_assignment_id;

  private UUID asset_type_id;

  private String asset_type_name;

  private UUID status_id;

  private String status_name;

  private AssignmentStatusType status_type;

  private Long asset_type_status_usage_count;

  private Boolean inherited_flag;

  private UUID parent_asset_type_id;

  private String parent_asset_type_name;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
