package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetRelationComponentAttributesResponse implements Response {
  private UUID attribute_type_id;

  private String attribute_type_name;

  private AttributeKindType attribute_kind;

  private String validation_mask;

  private String[] allowed_values;

  private UUID relation_component_attribute_id;

  private String value;
}
