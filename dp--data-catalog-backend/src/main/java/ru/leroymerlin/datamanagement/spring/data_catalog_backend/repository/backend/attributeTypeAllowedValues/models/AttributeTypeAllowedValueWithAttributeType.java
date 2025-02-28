package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttributeTypeAllowedValueWithAttributeType {
  private UUID attributeTypeId;

  private UUID attributeTypeAllowedValueId;

  private String value;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;
}
