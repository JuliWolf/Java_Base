package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetRelationTypeAttributeTypesUsageCountParams {
  private UUID relationTypeId;

  private UUID attributeTypeId;

  private Integer pageSize;

  private Integer pageNumber;

  private SortField sortField;

  private SortOrder sortOrder;
}
