package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHistory;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetHistory;

/**
 * @author juliwolf
 */

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, UUID> {
  @Modifying
  @Query(value = """
    UPDATE asset_history
    SET
     valid_to = :newValidTo
    Where
      asset_id = :assetId and
      valid_to = :validTo
  """, nativeQuery = true)
  void updateLastAssetHistory (
    @Param("newValidTo") java.sql.Timestamp newValidTo,
    @Param("assetId") UUID assetId,
    @Param("validTo") java.sql.Timestamp validTo
  );

  @Modifying
  @Query(value = """
    UPDATE asset_history
    SET
     valid_to = sub.deleted_on
    From (
      Select deleted_on
      From asset
      Where asset_id = :assetId
    ) sub
    Where
      asset_id = :assetId and
      valid_to = :validTo
  """, nativeQuery = true)
  void updateLastAssetHistoryByDeletedAssetId (
    @Param("assetId") UUID assetId,
    @Param("validTo") java.sql.Timestamp validTo
  );

  @Modifying
  @Query(value = """
    INSERT INTO asset_history (
      asset_history_id, asset_id,
      asset_name, asset_type_id,
      asset_displayname, source_language,
      lifecycle_status, stewardship_status,
      created_on, created_by,
      last_modified_on, last_modified_by,
      deleted_flag, deleted_on, deleted_by,
      valid_from, valid_to
    )
    Select
      :assetHistoryId, :assetId,
      a.asset_name, a.asset_type_id,
      a.asset_displayname, a.source_language,
      a.lifecycle_status, a.stewardship_status,
      a.created_on, a.created_by,
      a.last_modified_on, a.last_modified_by,
      a.deleted_flag, a.deleted_on, a.deleted_by,
      a.deleted_on, a.deleted_on
    From asset a
    Where a.asset_id = :assetId
  """, nativeQuery = true)
  void createAssetHistoryFromAsset (
    @Param("assetHistoryId") UUID assetHistoryId,
    @Param("assetId") UUID assetId
  );
}
