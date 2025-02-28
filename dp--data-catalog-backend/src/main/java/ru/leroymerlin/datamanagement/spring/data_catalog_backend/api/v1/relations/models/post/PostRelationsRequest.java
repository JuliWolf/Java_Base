package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.interfaces.RelationTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostRelationsRequest implements Request, RelationTypeRequest {
  private String relation_type_id;

  private List<PostRelationRequest> component = new ArrayList<>();
}
