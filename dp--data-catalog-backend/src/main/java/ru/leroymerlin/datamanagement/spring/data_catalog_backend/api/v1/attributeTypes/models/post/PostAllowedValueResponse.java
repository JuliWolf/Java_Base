package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PostAllowedValueResponse {
  private UUID value_id;

  private String value;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
