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
public class GetBusinessFunctionsResponse implements Response {
  private long total;

  private int pageSize;

  private int pageNumber;

  private List<GetBusinessFunctionResponse> businessFunctions;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class GetBusinessFunctionResponse implements Response {
    private UUID businessFunctionId;

    private String businessFunctionName;

    private String businessFunctionDescription;

    private Set<String> businessFunctionSynonyms;

    private BusinessFunctionExecutor businessFunctionExecutor;

    private List<BusinessFunctionOwner> businessFunctionOwners;

    private Set<String> businessFunctionDomainIds;

    public GetBusinessFunctionResponse (
      UUID businessFunctionId,
      String businessFunctionName,
      String businessFunctionDescription,
      Set<String> businessFunctionSynonyms,
      String structuralUnitNumber,
      Set<String> ownerLdap,
      Set<String> businessFunctionDomainIds
    ) {
      this.businessFunctionId = businessFunctionId;
      this.businessFunctionName = businessFunctionName;
      this.businessFunctionDescription = businessFunctionDescription;
      this.businessFunctionSynonyms = businessFunctionSynonyms;
      this.businessFunctionExecutor = new BusinessFunctionExecutor(structuralUnitNumber);
      this.businessFunctionOwners = ownerLdap != null
        ? ownerLdap.stream().map(BusinessFunctionOwner::new).toList()
        : null;
      this.businessFunctionDomainIds = businessFunctionDomainIds;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class BusinessFunctionExecutor {
      private String structuralUnitNumber;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class BusinessFunctionOwner {
      private Ldap digitalIdentities;

      public BusinessFunctionOwner (String digitalIdentities) {
        this.digitalIdentities = new Ldap(digitalIdentities);
      }

      @AllArgsConstructor
      @NoArgsConstructor
      @Getter
      @Setter
      public static class Ldap {
        private String ldap;
      }
    }
  }
}
