package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.DeleteAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.PatchAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.PostAsset;

/**
 * @author juliwolf
 */

public interface StageAssetRepository extends JpaRepository<StageAsset, Long>  {
  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision(
      sa.actionDecision, count(*)
    )
    from StageAsset sa
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
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.PatchAsset(
      sa.stageAssetId, sa.matchedAssetId, sa.assetName, sa.assetDisplayName,
      sa.assetTypeId, sa.lifecycleStatusId, sa.stewardshipStatusId
    )
    from StageAsset sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """, countQuery = """
    Select count(sa.matchedAssetId)
    from StageAsset sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """)
  List<PatchAsset> findAllPatchStageAssetByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.PostAsset(
      sa.extractJob.jobId, sa.stageAssetId, sa.assetName, sa.assetDisplayName,
      sa.assetTypeId, sa.lifecycleStatusId, sa.stewardshipStatusId
    )
    from StageAsset sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """, countQuery = """
    Select count(sa.stageAssetId)
    from StageAsset sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """)
  List<PostAsset> findAllPostStageAssetByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.DeleteAsset(
      sa.stageAssetId, sa.matchedAssetId
    )
    from StageAsset sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """, countQuery = """
    Select count(sa.matchedAssetId)
    from StageAsset sa
    Where
      sa.extractJob.jobId = :jobId and
      sa.actionDecision = :actionDecision
  """)
  List<DeleteAsset> findAllDeleteStageAssetByJobIdPageable(
    @Param("jobId") UUID jobId,
    @Param("actionDecision") ActionDecision actionDecision,
    Pageable pageable
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_asset
    Set
      action_process_status = :processStatus,
      action_processed_datetime = current_timestamp
    WHERE
      stage_asset.stage_asset_id in :stageAssetIds
  """, nativeQuery = true)
  void setStageAssetsProcessStatus (
    @Param("processStatus") String processStatus,
    @Param("stageAssetIds") List<Long> stageAssetIds
  );

  @Modifying
  @Query(value = """
    UPDATE extract_delta.stage_asset
    Set
      created_asset_id = :createdAssetId,
      action_process_status = :processStatus,
      action_processed_datetime = current_timestamp
    WHERE
      stage_asset.stage_asset_id = :stageAssetId
  """, nativeQuery = true)
  void setStageAssetsProcessStatus (
    @Param("createdAssetId") UUID createdAssetId,
    @Param("processStatus") String processStatus,
    @Param("stageAssetId") Long stageAssetId
  );

  @Modifying
  @Query(value = """
    Delete From extract_delta.stage_asset
    WHERE
      stage_asset.job_id in :jobIds
  """, nativeQuery = true)
  void deleteStageAssetsByJobIds (
    @Param("jobIds") Set<UUID> jobIds
  );
}
