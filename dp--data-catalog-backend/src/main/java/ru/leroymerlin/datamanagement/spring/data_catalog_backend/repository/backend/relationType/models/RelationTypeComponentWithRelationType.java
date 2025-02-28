package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.models;

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
@Setter
@Getter
public class RelationTypeComponentWithRelationType {
  private UUID relationTypeComponentId;

  private UUID relationTypeId;
}
