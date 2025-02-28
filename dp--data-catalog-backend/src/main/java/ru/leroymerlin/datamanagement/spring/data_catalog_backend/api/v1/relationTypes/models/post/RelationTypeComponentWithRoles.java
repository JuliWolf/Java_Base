package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RelationTypeComponentWithRoles {
  protected String responsibility_inheritance_role;

  protected String hierarchy_role;
}
