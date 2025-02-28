package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models;

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
public class RelationWithRelationType {
  private UUID relationId;

  private UUID relationTypeId;

  private String relationTypeName;

  private Boolean responsibilityInheritanceFlag;

  private Boolean hierarchyFlag;

  private Boolean uniquenessFlag;

  private java.sql.Timestamp relationCreatedOn;

  private UUID relationCreatedBy;
}
