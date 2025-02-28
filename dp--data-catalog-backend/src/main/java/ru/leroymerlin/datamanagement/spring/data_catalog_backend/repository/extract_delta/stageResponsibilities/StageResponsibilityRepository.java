package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models.DeleteResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models.PostResponsibility;

/**
 * @author juliwolf
 */

public interface StageResponsibilityRepository extends JpaRepository<StageResponsibility, Long>  {
  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision(
      sr.actionDecision, count(*)
    )
    from StageResponsibility sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision <> 'N' and
      sr.actionProcessStatus is null
    group by sr.actionDecision
  """)
  List<StageCountByDecision> countActionDecisionsByJobId(
    @Param("jobId") UUID jobId
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models.PostResponsibility(
      sr.stageResponsibilityId,
      sr.assetId, sr.roleId,
      sr.userId, sr.groupId, sr.responsibleType
    )
    from StageResponsibility sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision = :actionDecision and
      sr.assetId is not null
  """, countQuery = """
    Select count(sr.stageResponsibilityId)
    from StageResponsibility sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision = :actionDecision and
      sr.assetId is not null
  """)
  List<PostResponsibility> findAllPostStageResponsibilityByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models.DeleteResponsibility(
      sr.stageResponsibilityId, sr.matchedResponsibilityId
    )
    from StageResponsibility sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision = :actionDecision
  """, countQuery = """
    Select count(sr.matchedResponsibilityId)
    from StageResponsibility sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision = :actionDecision
  """)
  List<DeleteResponsibility> findAllDeleteStageResponsibilityByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_responsibility
    Set
      action_process_status = :processStatus,
      action_processed_datetime = current_timestamp
    WHERE
      stage_responsibility.stage_responsibility_id in :stageResponsibilityIds
  """, nativeQuery = true)
  void setStageResponsibilitiesProcessStatus (
    @Param("processStatus") String processStatus,
    @Param("stageResponsibilityIds") List<Long> stageResponsibilityIds
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_responsibility
    Set
      asset_id = :assetId
    WHERE
      stage_responsibility.asset_name_nk = :assetName and
      stage_responsibility.asset_id is null and
      stage_responsibility.action_decision = 'I' and
      stage_responsibility.action_process_status is null and
      stage_responsibility.job_id = :jobId
  """, nativeQuery = true)
  void setStageAssetId (
    @Param("jobId") UUID jobId,
    @Param("assetId") UUID assetId,
    @Param("assetName") String assetName
  );

  @Modifying
  @Query(value = """
    Delete From extract_delta.stage_responsibility
    WHERE
      stage_responsibility.job_id in :jobIds
  """, nativeQuery = true)
  void deleteStageResponsibilitiesByJobIds (
    @Param("jobIds") Set<UUID> jobIds
  );
}
