package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.models.ParentChildAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetHierarchy;

/**
 * @author JuliWolf
 */
public interface AssetHierarchyRepository extends JpaRepository<AssetHierarchy, UUID> {
  @Query(value ="""
    SELECT count(ar.assetHierarchyId) > 0 FROM AssetHierarchy ar
    WHERE
      ar.childAsset.assetId = :childAssetId and
      ar.parentAsset.assetId = :parentAssetId and
      ar.isDeleted = false
  """)
  Boolean isParentChildAssetsConnectionExists (
    @Param("childAssetId") UUID childAssetId,
    @Param("parentAssetId") UUID parentAssetId
  );

  @Query(value ="""
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.models.ParentChildAsset(
      ar.parentAsset.assetId, ar.childAsset.assetId
    )
    FROM AssetHierarchy ar
    WHERE
      ar.childAsset.assetId in :childAssetIds and
      ar.parentAsset.assetId in :parentAssetIds and
      ar.isDeleted = false
  """)
  List<ParentChildAsset> findAllByChildParentAssets (
    @Param("childAssetIds") List<UUID> childAssetIds,
    @Param("parentAssetIds") List<UUID> parentAssetIds
  );

  @Query(value ="""
    SELECT ar FROM AssetHierarchy ar
    WHERE
      ar.relation.relationId in :relationIds and
      ar.isDeleted = false
  """)
  List<AssetHierarchy> findAllByRelationIds (
    @Param("relationIds") List<UUID> relationIds
  );

  @Modifying
  @Query(value ="""
    UPDATE asset_hierarchy
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE
      relation_id in :relationIds
  """, nativeQuery = true)
  void deleteAllByRelationIds (
    @Param("relationIds") List<UUID> relationIds,
    @Param("userId") UUID userId
  );
}
