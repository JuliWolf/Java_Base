package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get;

import java.util.List;
import java.util.Set;
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
public class GetBusinessTermsResponse implements Response {
  private long total;

  private int pageSize;

  private int pageNumber;

  private List<GetBusinessTermResponse> businessTerms;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class GetBusinessTermResponse implements Response {
    private UUID businessTermId;

    private String businessTermName;

    private String businessTermTechnicalName;

    private String businessTermDescription;

    private Set<String> businessTermSynonyms;

    private BusinessOwner businessOwner;

    public GetBusinessTermResponse (
      UUID businessTermId,
      String businessTermName,
      String businessTermTechnicalName,
      String businessTermDescription,
      Set<String> businessTermSynonyms,
      String ldap
    ) {
      this.businessTermId = businessTermId;
      this.businessTermName = businessTermName;
      this.businessTermTechnicalName = businessTermTechnicalName;
      this.businessTermDescription = businessTermDescription;
      this.businessTermSynonyms = businessTermSynonyms;
      this.businessOwner = new BusinessOwner(ldap);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class BusinessOwner implements Response {
      private Ldap digitalIdentities;

      public BusinessOwner (String ldap) {
        this.digitalIdentities = new Ldap(ldap);
      }

      @AllArgsConstructor
      @NoArgsConstructor
      @Getter
      @Setter
      public static class Ldap implements Response {
        private String ldap;
      }
    }
  }
}
