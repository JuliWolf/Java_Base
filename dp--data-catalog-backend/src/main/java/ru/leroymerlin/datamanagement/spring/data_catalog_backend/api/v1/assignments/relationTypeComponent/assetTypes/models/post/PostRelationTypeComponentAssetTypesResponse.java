package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post;

import java.util.List;
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
public class PostRelationTypeComponentAssetTypesResponse implements Response {
  private List<PostRelationTypeComponentAssetTypeResponse> relation_type_component_asset_types_assignment;
}
