package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get;

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
public class GetRelationResponse implements Response {
  private UUID relation_id;

  private UUID relation_type_id;

  private String relation_type_name;

  private Boolean responsibility_inheritance_flag;

  private Boolean hierarchy_flag;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private List<GetRelationComponentResponse> relation_components;
}
