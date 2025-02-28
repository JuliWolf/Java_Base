package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRelationTypeAttributeTypeResponse {
  private UUID relation_type_attribute_type_id;

  private UUID relation_type_id;

  private UUID attribute_type_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
