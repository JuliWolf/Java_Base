package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post;

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
@Setter
@Getter
public class PostRelationComponentAttributeResponse implements Response {
  private UUID relation_component_attribute_id;

  private UUID attribute_type_id;

  private UUID relation_component_id;

  private String value;

  private Boolean integer_flag;

  private Double value_numeric;

  private Boolean value_bool;

  private java.sql.Timestamp value_datetime;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
