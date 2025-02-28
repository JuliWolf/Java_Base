package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.models.get;

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

    private String businessTermDefinition;

    private Set<String> businessTermSynonyms;

    private List<DigitalProduct> digitalProducts;

    private List<BusinessOwner> businessOwners;

    public GetBusinessTermResponse (
      UUID businessTermId,
      String businessTermName,
      String businessTermTechnicalName,
      String businessTermDefinition,
      Set<String> digitalProducts,
      Set<String> businessTermSynonyms,
      Set<String> ldaps
    ) {
      this.businessTermId = businessTermId;
      this.businessTermName = businessTermName;
      this.businessTermTechnicalName = businessTermTechnicalName;
      this.businessTermDefinition = businessTermDefinition;
      this.businessTermSynonyms = businessTermSynonyms;
      this.digitalProducts = digitalProducts != null ? digitalProducts.stream().map(DigitalProduct::new).toList() : null;
      this.businessOwners = ldaps != null ? ldaps.stream().map(BusinessOwner::new).toList() : null;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class DigitalProduct implements Response {
      private String digitalProductCode;
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
