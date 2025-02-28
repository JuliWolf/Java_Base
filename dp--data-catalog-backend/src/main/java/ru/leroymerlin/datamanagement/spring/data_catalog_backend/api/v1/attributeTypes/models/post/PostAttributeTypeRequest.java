package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post;

import java.util.ArrayList;
import java.util.List;
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
public class PostAttributeTypeRequest implements Request {

  private String attribute_type_name;

  private String attribute_type_description;

  private AttributeKindType attribute_type_kind;

  private String validation_mask;

  private List<String> attribute_type_allowed_values = new ArrayList<>();

  private String rdm_table_id;
}
