package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetAssetTypeStatusAssignmentResponse {
  private UUID asset_status_id;

  private UUID status_id;

  private String status_name;

  private String status_description;

  private Boolean inherited_flag;

  private UUID parent_asset_type_id;

  private String parent_asset_type_name;

  private AssignmentStatusType status_type;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
