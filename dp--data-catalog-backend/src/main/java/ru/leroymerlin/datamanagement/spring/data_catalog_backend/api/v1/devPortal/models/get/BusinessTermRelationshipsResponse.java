package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get;

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
public class BusinessTermRelationshipsResponse implements Response {
  private long total;

  private int pageSize;

  private int pageNumber;

  private List<BusinessTermRelationshipResponse> businessTermRelationships;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class BusinessTermRelationshipResponse implements Response {
    private UUID businessTermId;

    private UUID businessTermRelationId;

    private String businessTermRelationshipName;

    private String businessTermRelationshipTechnicalName;

    private RelatedBusinessTerm relatedBusinessTerm;

    private BusinessRelationCardinality businessTermRelationCardinality;

    public BusinessTermRelationshipResponse (
      UUID businessTermId,
      UUID businessTermRelationId,
      String businessTermRelationshipName,
      String businessTermRelationshipTechnicalName,
      UUID relatedBusinessTermId,
      String businessTermRelationCardinality
    ) {
      this.businessTermId = businessTermId;
      this.businessTermRelationId = businessTermRelationId;
      this.businessTermRelationshipName = businessTermRelationshipName;
      this.businessTermRelationshipTechnicalName = businessTermRelationshipTechnicalName;
      this.relatedBusinessTerm = new RelatedBusinessTerm(relatedBusinessTermId);
      this.businessTermRelationCardinality = businessTermRelationCardinality != null ? BusinessRelationCardinality.fromString(businessTermRelationCardinality) : null;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class RelatedBusinessTerm implements Response {
      private UUID businessTermId;
    }
  }
}
