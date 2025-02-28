package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.AttributeTypeResponse;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GetAttributeTypeResponse extends AttributeTypeResponse {
  private Long attribute_type_usage_count;

  public GetAttributeTypeResponse (AttributeTypeResponse attributeTypeResponse, Long attributeTypeUsageCount) {
    super(
      attributeTypeResponse.getAttribute_type_id(),
      attributeTypeResponse.getAttribute_type_name(),
      attributeTypeResponse.getAttribute_type_description(),
      attributeTypeResponse.getAttribute_kind(),
      attributeTypeResponse.getValidation_mask(),
      attributeTypeResponse.getSource_language(),
      attributeTypeResponse.getRdm_table_id(),
      attributeTypeResponse.getCreated_on(),
      attributeTypeResponse.getCreated_by(),
      attributeTypeResponse.getLast_modified_on(),
      attributeTypeResponse.getLast_modified_by(),
      attributeTypeResponse.getAllowed_values()
    );

    this.attribute_type_usage_count = attributeTypeUsageCount;
  }
}
