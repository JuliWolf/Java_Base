package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get;

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
@Setter
@Getter
public class GetAssetTypeAttributeTypesAssignmentsResponse implements Response {
  private UUID asset_type_id;

  private String asset_type_name;

  private List<GetAssetTypeAttributeTypeAssignmentResponse> asset_attribute_assignment;
}
