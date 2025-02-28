package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get;

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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetRelationTypeComponentAttributeTypeResponse implements Response {
  private UUID relation_type_component_attribute_type_id;

  private UUID attribute_type_id;

  private String attribute_type_name;

  private AttributeKindType attribute_kind;

  private String validation_mask;

  private String[] allowed_values;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
