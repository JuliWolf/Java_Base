package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtractFilters;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtractFilters.models.MetadataExtractFilterAirflowData;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.MetadataExtractFilter;

/**
 * @author juliwolf
 */

public interface MetadataExtractFilterRepository extends JpaRepository<MetadataExtractFilter, UUID> {
  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtractFilters.models.MetadataExtractFilterAirflowData(
      mef.metadataExtract.metadataExtractId,
      mef.metadataExtractFilterId,
      mef.metadataFilterType, mef.objectType,
      mef.conditionType, mef.value
    )
    FROM MetadataExtractFilter mef
    WHERE
      mef.metadataExtract.metadataExtractId in :metadataExtractIds and
      mef.isDeleted = false
  """)
  List<MetadataExtractFilterAirflowData> findAllByExtractIds (
    @Param("metadataExtractIds") List<UUID> metadataExtractIds
  );
}
