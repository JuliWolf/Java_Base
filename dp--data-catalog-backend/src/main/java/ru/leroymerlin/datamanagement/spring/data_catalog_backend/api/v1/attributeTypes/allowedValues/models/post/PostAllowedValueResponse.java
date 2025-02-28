package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PostAllowedValueResponse implements Response {
  private UUID value_id;

  private UUID attribute_type_id;

  private String value;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
