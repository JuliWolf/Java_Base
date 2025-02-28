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
public class PostAssetTypeStatusAssignmentRequest {
  private AssignmentStatusType status_type;

  private UUID status_id;
}
