package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RelationTypeResponse implements Response {
  private UUID relation_type_id;

  private String relation_type_name;

  private String relation_type_description;

  private Integer component_number;

  private Boolean responsibility_inheritance_flag;

  private Boolean hierarchy_flag;

  private Boolean uniqueness_flag;

  private Boolean self_related_flag;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;

  private List<RelationTypeComponentResponse> relation_type_component;
}
