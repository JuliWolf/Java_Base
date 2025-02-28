package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PatchAttributeTypeRequest implements Request {
  private Optional<String> attribute_type_name;

  private Optional<String> attribute_type_description;

  private AttributeKindType attribute_kind;

  private Optional<String> validation_mask;

  private Optional<String> rdm_table_id;
}
