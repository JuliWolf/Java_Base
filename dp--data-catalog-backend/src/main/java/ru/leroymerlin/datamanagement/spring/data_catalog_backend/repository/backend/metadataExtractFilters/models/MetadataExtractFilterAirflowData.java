package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtractFilters.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ConditionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataFilterType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MetadataExtractFilterAirflowData {
  private UUID metadataExtractId;

  private UUID metadataExtractFilterId;

  private MetadataFilterType filterType;

  private String objectType;

  private ConditionType conditionType;

  private String value;
}
