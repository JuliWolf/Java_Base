package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAllowedValueResponse;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AttributeTypeResponse implements Response {
  protected UUID attribute_type_id;

  private String attribute_type_name;

  private String attribute_type_description;

  private AttributeKindType attribute_kind;

  private String validation_mask;

  private String source_language;

  private String rdm_table_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;

  private List<PostAllowedValueResponse> allowed_values;
}
