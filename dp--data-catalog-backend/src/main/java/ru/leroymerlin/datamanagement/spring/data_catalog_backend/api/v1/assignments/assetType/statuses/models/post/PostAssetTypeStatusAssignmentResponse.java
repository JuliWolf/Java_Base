package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post;

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
public class PostAssetTypeStatusAssignmentResponse {
  private UUID asset_status_id;

  private UUID asset_type_id;

  private AssignmentStatusType status_type;

  private UUID status_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
