package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.models.get;

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
public class BusinessTermRelationshipsRequest {
  private UUID businessTermId;

  private String businessTermRelationshipName;

  private String businessTermRelationshipTechnicalName;

  private BusinessRelationCardinality businessTermRelationCardinality;

  private Integer pageSize;

  private Integer pageNumber;
}
