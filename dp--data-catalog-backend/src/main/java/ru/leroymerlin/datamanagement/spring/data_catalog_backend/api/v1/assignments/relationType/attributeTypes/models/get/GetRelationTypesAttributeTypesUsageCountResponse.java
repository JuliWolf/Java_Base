package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetRelationTypesAttributeTypesUsageCountResponse implements Response {
  long total;

  int page_size;

  int page_number;

  List<GetRelationTypesAttributeTypeUsageCountResponse> results;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class GetRelationTypesAttributeTypeUsageCountResponse implements Response {
    private UUID relation_type_attribute_type_assignment_id;

    private UUID relation_type_id;

    private String relation_type_name;

    private UUID attribute_type_id;

    private String attribute_type_name;

    private Long relation_type_attribute_type_usage_count;

    private java.sql.Timestamp created_on;

    private UUID created_by;
  }
}
