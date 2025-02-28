package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.models.ActiveExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;

/**
 * @author juliwolf
 */

public interface ExtractJobRepository extends JpaRepository<ExtractJob, UUID> {
  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.models.ActiveExtractJob(
      ej.jobId, ej.kafkaTopic, ej.tablesCount, ej.sourceType,
      ej.sourceName, ej.jobStatus, ej.fullMetaFlag, ej.rootAsset.assetId,
      ej.lastStatusChangeDatetime
    )
    from ExtractJob ej
    Where
      (ej.jobErrorFlag is null or ej.jobErrorFlag = false) and
      ej.jobStatus != 'SUCCESS'
  """, countQuery = """
    Select count(ej.jobId)
    from ExtractJob ej
    Where
      (ej.jobErrorFlag is null or ej.jobErrorFlag = false) and
      ej.jobStatus != 'SUCCESS'
  """)
  Page<ActiveExtractJob> findAllActiveJobsPageable(
    Pageable pageable
  );

  @Query(value = """
    Select ej
    from ExtractJob ej
    Where
      (ej.jobErrorFlag is null or ej.jobErrorFlag = false) and
      ej.jobStatus <> 'SUCCESS'
  """)
  List<ExtractJob> findActiveJobs();

  @Query(value = """
    Select ej
    from ExtractJob ej
    Where
      (ej.jobErrorFlag is null or ej.jobErrorFlag = false) and
      (ej.jobStatus = 'STAGE_COMPLETE' or ej.jobStatus = 'API_START')
  """)
  List<ExtractJob> findJobsToUpdate ();

  @Modifying
  @Query(value = """
    Update ExtractJob
    Set
      jobError = 'Timeout reached, Extra Job failed',
      jobErrorFlag = true
    Where
      lastStatusChangeDatetime <= :maxDateTime and
      (jobErrorFlag is null or jobErrorFlag = false) and
      jobStatus <> 'SUCCESS'
  """)
  void updateExpiredJobs(
    @Param(value = "maxDateTime") Timestamp maxDateTime
  );
}
