package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post;

import java.util.List;
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
public class PostAssetTypeAttributesAssignmentsResponse implements Response {
  private List<PostAssetTypeAttributeAssignmentResponse> asset_attribute_assignment;
}
