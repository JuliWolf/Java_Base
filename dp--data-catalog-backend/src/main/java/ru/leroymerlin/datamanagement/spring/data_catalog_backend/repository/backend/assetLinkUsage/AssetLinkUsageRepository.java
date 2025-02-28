package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetLinkUsage;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetLinkUsage;

/**
 * @author JuliWolf
 */
public interface AssetLinkUsageRepository extends JpaRepository<AssetLinkUsage, UUID> {
  @Query("""
    Select alu
    From AssetLinkUsage alu
    Where
      (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null or alu.asset.assetId = :assetId) and
      (cast(:attributeId as org.hibernate.type.PostgresUUIDType) is null or alu.attribute.attributeId = :attributeId) and
      alu.isDeleted = false
  """)
  List<AssetLinkUsage> findAllByParams(
    @Param("assetId") UUID assetId,
    @Param("attributeId") UUID attributeId
  );

  @Modifying
  @Query(value = """
    UPDATE asset_link_usage
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      link_asset_id in :assetIds
  """, nativeQuery = true)
  void deleteAssetLinkByAssetIds(
    @Param("assetIds") List<UUID> assetIds,
    @Param("userId") UUID userId
  );

  @Modifying
  @Query(value = """
    UPDATE asset_link_usage
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      attribute_id in :attributeIds
  """, nativeQuery = true)
  void deleteAllByAttributeId(
    @Param("attributeIds") List<UUID> attributeIds,
    @Param("userId") UUID userId
  );
}
