package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatchAttributeResponse implements Response {
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

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;
}
