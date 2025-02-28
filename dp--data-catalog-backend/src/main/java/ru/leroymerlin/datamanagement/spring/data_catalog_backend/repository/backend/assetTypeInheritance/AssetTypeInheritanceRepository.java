package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypeInheritance;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.models.AssetTypeChild;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeInheritance;

public interface AssetTypeInheritanceRepository extends JpaRepository<AssetTypeInheritance, UUID> {
  @Query(value = """
    SELECT count(ati.inheritanceId) > 0 FROM AssetTypeInheritance ati
    WHERE
      (cast(:parentAssetTypeId as org.hibernate.type.PostgresUUIDType) is null or ati.parentAssetType.assetTypeId = :parentAssetTypeId) and
      (cast(:childAssetTypeId as org.hibernate.type.PostgresUUIDType) is null or ati.childAssetType.assetTypeId = :childAssetTypeId) and
      ati.isDeleted = false
  """)
  Boolean isAssetTypeInheritanceExistsByParentAssetTypeIdAndChildAssetTypeId (
    @Param("parentAssetTypeId") UUID parentAssetTypeId,
    @Param("childAssetTypeId") UUID childAssetTypeId
  );

  @Query(value = """
    SELECT ati
    FROM AssetTypeInheritance ati
    WHERE
      ati.childAssetType.assetTypeId = :childAssetTypeId and
      ati.isDeleted = false
  """)
  List<AssetTypeInheritance> findAllAssetTypeInheritanceExistsByChildAssetTypeId (
    @Param("childAssetTypeId") UUID childAssetTypeId
  );

  @Modifying
  @Query(value = """
    UPDATE asset_type_inheritance
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE
      child_asset_type_id = :childAssetTypeId
  """, nativeQuery = true)
  void deleteByChildAssetTypeId (
    @Param("childAssetTypeId") UUID childAssetTypeId,
    @Param("userId") UUID userId
  );
}
