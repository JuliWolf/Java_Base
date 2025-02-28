package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataExtractStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataSourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataSourceType;

/**
 * @author juliwolf
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetMetadataExtractRequestParams {
  private Integer pageSize;

  private Integer pageNumber;

  private MetadataSourceKind metadataSourceKind;

  private MetadataSourceType metadataSourceType;

  private MetadataExtractStatus extractStatus;

  private Boolean fullMetaFlag;
}
