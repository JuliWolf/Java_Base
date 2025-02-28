package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.get;

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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetRelationTypeComponentAssetTypeAssignmentsResponse implements Response {
  private UUID relation_type_component_id;

  private String relation_type_component_name;

  private List<GetRelationTypeComponentAssetTypeAssignmentResponse> relation_type_component_allowed_asset_type;
}
