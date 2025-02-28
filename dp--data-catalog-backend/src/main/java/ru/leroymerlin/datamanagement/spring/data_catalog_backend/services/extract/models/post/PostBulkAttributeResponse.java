package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PostBulkAttributeResponse {
  private UUID attribute_id;

  private UUID attribute_type_id;

  private UUID asset_id;

  private String value;

  private Boolean integer_flag;

  private Double value_numeric;

  private Boolean value_bool;

  private java.sql.Timestamp value_datetime;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
