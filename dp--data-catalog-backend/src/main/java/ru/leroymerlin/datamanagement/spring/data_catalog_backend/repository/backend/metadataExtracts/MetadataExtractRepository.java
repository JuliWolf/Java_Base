package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtracts;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtracts.models.MetadataExtractAirflowData;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.MetadataExtract;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataExtractStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataSourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataSourceType;

/**
 * @author juliwolf
 */

public interface MetadataExtractRepository extends JpaRepository<MetadataExtract, UUID> {
  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtracts.models.MetadataExtractAirflowData(
      me.metadataExtractId,
      a.assetId, a.assetName,
      me.extractScheduleCron, me.extractStatus,
      me.airflowDag, me.kafkaTopic,
      me.metadataSourceKind, me.metadataSourceType,
      me.connectionInfo, me.vaultSecrets,
      me.fullMetaFlag
    )
    FROM MetadataExtract me
    Inner Join Asset a on a.assetId = me.asset.assetId
    WHERE
      (:metadataSourceKind is null or me.metadataSourceKind = :metadataSourceKind) and
      (:metadataSourceType is null or me.metadataSourceType = :metadataSourceType) and
      (:extractStatus is null or me.extractStatus = :extractStatus) and
      (:fullMetaFlag is null or me.fullMetaFlag = :fullMetaFlag) and
      me.isDeleted = false
  """, countQuery = """
    SELECT count(me.metadataExtractId)
    FROM MetadataExtract me
    WHERE
      (:metadataSourceKind is null or me.metadataSourceKind = :metadataSourceKind) and
      (:metadataSourceType is null or me.metadataSourceType = :metadataSourceType) and
      (:extractStatus is null or me.extractStatus = :extractStatus) and
      (:fullMetaFlag is null or me.fullMetaFlag = :fullMetaFlag) and
      me.isDeleted = false
  """)
  Page<MetadataExtractAirflowData> findAllByParams (
    @Param("metadataSourceKind") MetadataSourceKind metadataSourceKind,
    @Param("metadataSourceType") MetadataSourceType metadataSourceType,
    @Param("extractStatus") MetadataExtractStatus extractStatus,
    @Param("fullMetaFlag") Boolean fullMetaFlag,
    Pageable pageable
  );
}
