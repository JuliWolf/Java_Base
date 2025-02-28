package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models.DeleteRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models.PostRelation;

/**
 * @author juliwolf
 */

public interface StageRelationRepository extends JpaRepository<StageRelation, Long>  {
  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision(
      sr.actionDecision, count(*)
    )
    from StageRelation sr
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
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models.PostRelation(
      sr.stageRelationId,
      sr.relationTypeId,
      sr.asset1Id, sr.relationTypeComponent1Id,
      sr.asset2Id, sr.relationTypeComponent2Id
    )
    from StageRelation sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision = :actionDecision and
      sr.asset1Id is not null and
      sr.asset2Id is not null
  """, countQuery = """
    Select count(sr.stageRelationId)
    from StageRelation sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision = :actionDecision and
      sr.asset1Id is not null and
      sr.asset2Id is not null
  """)
  List<PostRelation> findAllPostStageRelationByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models.DeleteRelation(
      sr.stageRelationId, sr.matchedRelationId
    )
    from StageRelation sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision = :actionDecision
  """, countQuery = """
    Select count(sr.matchedRelationId)
    from StageRelation sr
    Where
      sr.extractJob.jobId = :jobId and
      sr.actionDecision = :actionDecision
  """)
  List<DeleteRelation> findAllDeleteStageRelationByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_relation
    Set
      action_process_status = :processStatus,
      action_processed_datetime = current_timestamp
    WHERE
      stage_relation.stage_relation_id in :stageRelationIds
  """, nativeQuery = true)
  void setStageRelationsProcessStatus (
    @Param("processStatus") String processStatus,
    @Param("stageRelationIds") List<Long> stageRelationIds
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_relation
    Set
      created_relation_id = :createdRelationId,
      action_process_status = :processStatus,
      action_processed_datetime = current_timestamp
    WHERE
      stage_relation.stage_relation_id = :stageRelationId
  """, nativeQuery = true)
  void setStageRelationProcessStatus (
    @Param("createdRelationId") UUID createdRelationId,
    @Param("processStatus") String processStatus,
    @Param("stageRelationId") Long stageRelationId
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_relation
    Set
      asset_1_id = :assetId
    WHERE
      stage_relation.asset_name_1_nk = :assetName and
      stage_relation.asset_1_id is null and
      stage_relation.action_decision = 'I' and
      stage_relation.action_process_status is null and
      stage_relation.job_id = :jobId
  """, nativeQuery = true)
  void setStageAssetIdForAsset1 (
    @Param("jobId") UUID jobId,
    @Param("assetId") UUID assetId,
    @Param("assetName") String assetName
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_relation
    Set
      asset_2_id = :assetId
    WHERE
      stage_relation.asset_name_2_nk = :assetName and
      stage_relation.asset_2_id is null and
      stage_relation.action_decision = 'I' and
      stage_relation.action_process_status is null and
      stage_relation.job_id = :jobId
  """, nativeQuery = true)
  void setStageAssetIdForAsset2 (
    @Param("jobId") UUID jobId,
    @Param("assetId") UUID assetId,
    @Param("assetName") String assetName
  );

  @Modifying
  @Query(value = """
    Delete From extract_delta.stage_relation
    WHERE
      stage_relation.job_id in :jobIds
  """, nativeQuery = true)
  void deleteStageRelationsByJobIds (
    @Param("jobIds") Set<UUID> jobIds
  );
}
