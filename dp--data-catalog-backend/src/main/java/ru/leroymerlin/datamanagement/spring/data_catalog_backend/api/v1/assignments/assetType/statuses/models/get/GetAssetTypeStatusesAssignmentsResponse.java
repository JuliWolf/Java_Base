package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetAssetTypeStatusesAssignmentsResponse implements Response {
  private UUID asset_type_id;

  private String asset_type_name;

  private List<GetAssetTypeStatusAssignmentResponse> asset_status_assignment;
}
