package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRelationTypeRequest implements Request {
  private String relation_type_name;

  private String relation_type_description;

  private Integer relation_type_component_number;

  private Boolean responsibility_inheritance_flag = false;

  private Boolean hierarchy_flag = false;

  private Boolean uniqueness_flag = true;

  private Boolean self_related_flag = false;

  private List<PostRelationTypeComponentRequest> relation_type_component;
}
