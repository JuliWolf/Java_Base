package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get;

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
public class BusinessTermAttributeRequest {
  private UUID businessTermId;

  private String businessAttributeName;

  private String businessAttributeTechnicalName;

  private BusinessAttributeDataType businessAttributeDataType;

  private BusinessAttributeConfidentiality businessAttributeConfidentiality;

  private Integer pageSize;

  private Integer pageNumber;
}
