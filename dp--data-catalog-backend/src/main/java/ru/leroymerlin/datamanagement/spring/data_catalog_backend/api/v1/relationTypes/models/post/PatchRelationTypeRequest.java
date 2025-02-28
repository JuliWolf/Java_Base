package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PatchRelationTypeRequest implements Request {
  private Optional<String> relation_type_name;

  private Optional<String> relation_type_description;

  private Boolean responsibility_inheritance_flag;

  private Boolean hierarchy_flag;

  private Boolean uniqueness_flag;

  private Boolean self_related_flag;

  private List<PatchRelationTypeComponentRequest> relation_type_component = new ArrayList<>();
}
