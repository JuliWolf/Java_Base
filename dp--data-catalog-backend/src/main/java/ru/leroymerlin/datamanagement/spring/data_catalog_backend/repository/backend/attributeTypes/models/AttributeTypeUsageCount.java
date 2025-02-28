package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AttributeTypeUsageCount {
  private UUID attributeTypeId;

  private Long count;
}
