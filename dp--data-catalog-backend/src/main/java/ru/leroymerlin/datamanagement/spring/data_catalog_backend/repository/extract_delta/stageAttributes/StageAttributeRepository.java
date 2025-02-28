package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.DeleteAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.PatchAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.PostAttribute;

/**
 * @author juliwolf
 */

public interface StageAttributeRepository extends JpaRepository<StageAttribute, Long>  {
  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision(
      sa.actionDecision, count(*)
    )
    from StageAttribute sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision <> 'N' and
      sa.actionProcessStatus is null
    group by sa.actionDecision
  """)
  List<StageCountByDecision> countActionDecisionsByJobId(
    @Param("jobId") UUID jobId
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.PatchAttribute(
      sa.stageAttributeId, sa.matchedAttributeId, sa.value
    )
    from StageAttribute sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """, countQuery = """
    Select count(sa.matchedAttributeId)
    from StageAttribute sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """)
  List<PatchAttribute> findAllPatchStageAttributeByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.PostAttribute(
      sa.stageAttributeId, sa.attributeTypeId, sa.assetId, sa.value
    )
    from StageAttribute sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision and
      sa.assetId is not null
  """, countQuery = """
    Select count(sa.stageAttributeId)
    from StageAttribute sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision and
      sa.assetId is not null
  """)
  List<PostAttribute> findAllPostStageAttributeByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.DeleteAttribute(
      sa.stageAttributeId, sa.matchedAttributeId
    )
    from StageAttribute sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """, countQuery = """
    Select count(sa.matchedAttributeId)
    from StageAttribute sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """)
  List<DeleteAttribute> findAllDeleteStageAttributeByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_attribute
    Set
      action_process_status = :processStatus,
      action_processed_datetime = current_timestamp
    WHERE
      stage_attribute.stage_attribute_id in :stageAttributeIds
  """, nativeQuery = true)
  void setStageAttributesProcessStatus (
    @Param("processStatus") String processStatus,
    @Param("stageAttributeIds") List<Long> stageAttributeIds
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_attribute
    Set
      created_attribute_id = :createdAttributeId,
      action_process_status = :processStatus,
      action_processed_datetime = current_timestamp
    WHERE
      stage_attribute.stage_attribute_id = :stageAttributeId
  """, nativeQuery = true)
  void setStageAttributesProcessStatus (
    @Param("createdAttributeId") UUID createdAttributeId,
    @Param("processStatus") String processStatus,
    @Param("stageAttributeId") Long stageAttributeId
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_attribute
    Set
      asset_id = :assetId
    WHERE
      stage_attribute.asset_name_nk = :assetName and
      stage_attribute.asset_id is null and
      stage_attribute.action_decision = 'I' and
      stage_attribute.action_process_status is null and
      stage_attribute.job_id = :jobId
  """, nativeQuery = true)
  void setStageAssetId (
    @Param("jobId") UUID jobId,
    @Param("assetId") UUID assetId,
    @Param("assetName") String assetName
  );

  @Modifying
  @Query(value = """
    Delete From extract_delta.stage_attribute
    WHERE
      stage_attribute.job_id in :jobIds
  """, nativeQuery = true)
  void deleteStageAttributesByJobIds (
    @Param("jobIds") Set<UUID> jobIds
  );
}
