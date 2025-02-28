package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRelationTypeAttributeTypesResponse implements Response {
  private List<PostRelationTypeAttributeTypeResponse> relation_attribute_assignment;
}
