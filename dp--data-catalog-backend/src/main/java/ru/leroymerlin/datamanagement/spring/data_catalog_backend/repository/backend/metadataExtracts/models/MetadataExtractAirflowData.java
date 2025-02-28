package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtracts.models;

import java.util.UUID;
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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MetadataExtractAirflowData {
  private UUID metadataExtractId;

  private UUID rootAssetId;

  private String rootAssetName;

  private String extractScheduleCron;

  private MetadataExtractStatus extractStatus;

  private String airflowDag;

  private String kafkaTopic;

  private MetadataSourceKind sourceKind;

  private MetadataSourceType sourceType;

  private String connectionInfo;

  private String vaultSecrets;

  private Boolean fullMetaFlag;
}
