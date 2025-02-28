package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJobObject;

/**
 * @author juliwolf
 */

public interface ExtractJobObjectRepository extends JpaRepository<ExtractJobObject, UUID> {
  @Query(value = """
    Select ejo
    from ExtractJobObject ejo
    Where
      ejo.extractJob.jobId in :jobs
  """)
  List<ExtractJobObject> findExtractJobObjectByJobs(
    @Param(value = "jobs") List<UUID> jobs
  );

  @Query(value = """
    Select ejo
    from ExtractJobObject ejo
    inner join ExtractJob ej on ej.jobId = ejo.extractJob.jobId
    Where
      (ej.jobStatus = 'SUCCESS' and ej.jobErrorFlag is null) or
      (ej.jobErrorFlag = true and ej.lastStatusChangeDatetime <= :maxDateTime)
  """)
  List<ExtractJobObject> findExtractJobObjectsOfFinishedJobs (
    @Param(value = "maxDateTime") Timestamp maxDateTime
  );
}
