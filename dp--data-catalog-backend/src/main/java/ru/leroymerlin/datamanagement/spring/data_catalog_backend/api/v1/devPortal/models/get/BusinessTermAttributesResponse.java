package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
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
public class BusinessTermAttributesResponse implements Response {
  private long total;

  private int pageSize;

  private int pageNumber;

  private List<GetBusinessAttributeResponse> businessAttributes;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class GetBusinessAttributeResponse implements Response {
    private UUID businessAttributeId;

    private String businessAttributeName;

    private String businessAttributeTechnicalName;

    private String businessAttributeDescription;

    private Set<String> businessAttributeSynonyms;

    private UUID businessTermId;

    private BusinessAttributeDataType businessAttributeDataType;

    private BusinessAttributeConfidentiality businessAttributeConfidentiality;

    private boolean businessAttributeIsPrimaryKey;

    public GetBusinessAttributeResponse (
      UUID businessAttributeId,
      String businessAttributeName,
      String businessAttributeTechnicalName,
      String businessAttributeDescription,
      Set<String> businessAttributeSynonyms,
      UUID businessTermId,
      String businessAttributeDataType,
      String businessAttributeConfidentiality,
      boolean businessAttributeIsPrimaryKey
    ) {
      this.businessAttributeId = businessAttributeId;
      this.businessAttributeName = businessAttributeName;
      this.businessAttributeTechnicalName = businessAttributeTechnicalName;
      this.businessAttributeDescription = businessAttributeDescription;
      this.businessAttributeSynonyms = businessAttributeSynonyms;
      this.businessTermId = businessTermId;
      this.businessAttributeIsPrimaryKey = businessAttributeIsPrimaryKey;

      parseBusinessAttributeDataType(businessAttributeDataType);
      parseBusinessAttributeConfidentiality(businessAttributeConfidentiality);
    }

    private void parseBusinessAttributeDataType (String value) {
      if (StringUtils.isEmpty(value)) return;

      this.businessAttributeDataType = BusinessAttributeDataType.valueOf(value);
    }

    private void parseBusinessAttributeConfidentiality (String value) {
      if (StringUtils.isEmpty(value)) return;

      this.businessAttributeConfidentiality = BusinessAttributeConfidentiality.valueOf(value);
    }
  }
}
