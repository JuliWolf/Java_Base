package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get;

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
public class GetAssetTypeAttributeTypeAssignmentsResponse implements Response {
  long total;

  int page_size;

  int page_number;

  List<GetAssetTypeAttributeTypeAssignmentResponse> results;

  @NoArgsConstructor
  @AllArgsConstructor
  @Setter
  @Getter
  public static class GetAssetTypeAttributeTypeAssignmentResponse implements Response {
    private UUID asset_type_attribute_type_assignment_id;

    private UUID asset_type_id;

    private String asset_type_name;

    private UUID attribute_type_id;

    private String attribute_type_name;

    private Long asset_type_attribute_type_usage_count;

    private Boolean inherited_flag;

    private UUID parent_asset_type_id;

    private String parent_asset_type_name;

    private java.sql.Timestamp created_on;

    private UUID created_by;
  }
}
