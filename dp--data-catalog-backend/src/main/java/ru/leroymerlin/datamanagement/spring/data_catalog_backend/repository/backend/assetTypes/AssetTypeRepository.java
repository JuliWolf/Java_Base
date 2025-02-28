package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.models.AssetTypeChild;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.models.AssetTypeWithRootFlag;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;

/**
 * @author JuliWolf
 */
public interface AssetTypeRepository extends JpaRepository<AssetType, UUID> {
  @Query(value = """
    SELECT
      cast(at.asset_type_id as text) as assetTypeIdText,
      at.asset_type_name as assetTypeName, at.asset_type_description as assetTypeDescription,
      l.language as languageName, at.asset_type_acronym as assetTypeAcronym, at.asset_type_color as assetTypeColor,
      at.asset_name_validation_mask as assetNameValidationMask,
      at.asset_name_validation_mask_example as assetNameValidationMaskExample,
      coalesce(ati.count, 0) as inheritanceCount,
      at.created_on as createdOn, cast(at.created_by as text) as createdByText,
      at.last_modified_on as lastModifiedOn, cast(at.last_modified_by as text) as lastModifiedByText
    FROM asset_type at
    Left join language l on at.source_language = l.language_id
    Left join (
      Select count(ati.inheritance_id) as count, at.asset_type_id
      From asset_type at
      Left JOIN asset_type_inheritance ati on ati.child_asset_type_id = at.asset_type_id
      GROUP BY at.asset_type_id
    ) ati on ati.asset_type_id = at.asset_type_id
    WHERE
      (:rootFlag is null or case
        when :rootFlag = true then (ati.count = 0)
        when :rootFlag = false then (ati.count > 0)
        end
      ) and
      (:assetTypeName is null or lower(at.asset_type_name) LIKE '%' || lower(:assetTypeName) || '%') and
      (:assetTypeDescription is null or lower(at.asset_type_description) LIKE '%' || lower(:assetTypeDescription) || '%') and
      at.deleted_flag = false
  """, countQuery = """
    SELECT count(at.asset_type_id)
    FROM asset_type at
    Left join language l on at.source_language = l.language_id
    Left join (
      Select count(ati.inheritance_id) as count, at.asset_type_id
      From asset_type at
      Left JOIN asset_type_inheritance ati on ati.child_asset_type_id = at.asset_type_id
      GROUP BY at.asset_type_id
    ) ati on ati.asset_type_id = at.asset_type_id
    WHERE
      (:rootFlag is null or case
        when :rootFlag = true then (ati.count > 0)
        when :rootFlag = false then (ati.count = 0)
        end
      ) and
      (:assetTypeName is null or lower(at.asset_type_name) LIKE '%' || lower(:assetTypeName) || '%') and
      (:assetTypeDescription is null or lower(at.asset_type_description) LIKE '%' || lower(:assetTypeDescription) || '%') and
      at.deleted_flag = false
  """, nativeQuery = true)
  Page<AssetTypeWithRootFlag> findAllByAssetTypeNameAndDescriptionPageable (
    @Param("rootFlag") Boolean rootFlag,
    @Param("assetTypeName") String assetTypeName,
    @Param("assetTypeDescription") String assetTypeDescription,
    Pageable pageable
  );

  @Query(value = """
    SELECT
      cast(ati.child_asset_type_id as text) as assetTypeIdText,
      at.asset_type_name as assetTypeName,
      at.asset_type_description as assetTypeDescription,
      asth.children_asset_type_count as childrenAssetTypeCount
    FROM asset_type_inheritance ati
    inner join asset_type at on at.asset_type_id = ati.child_asset_type_id
    left join (
      select
        count(*) as children_asset_type_count,
        parent_asset_type_id
      from asset_type_inheritance ati2
      where deleted_flag = false
      group by parent_asset_type_id
      ) asth on asth.parent_asset_type_id = ati.child_asset_type_id
    WHERE
      ati.parent_asset_type_id = :assetTypeId and
      at.deleted_flag = false
  """, countQuery = """
    SELECT count(*)
    FROM asset_type_inheritance ati
    inner join asset_type at on at.asset_type_id = ati.child_asset_type_id
    WHERE
      ati.parent_asset_type_id = :assetTypeId and
      at.deleted_flag = false
  """, nativeQuery = true)
  Page<AssetTypeChild> findAllAssetTypeChildrenPageable (
    @Param("assetTypeId") UUID assetTypeId,
    Pageable pageable
  );

  boolean existsByAssetTypeIdAndIsDeletedFalse(UUID assetTypeId);

  @Query(value= """
    WITH RECURSIVE tree(child, parent) AS (
      SELECT ath.child_asset_type_id, ath.parent_asset_type_id
      FROM asset_type_inheritance ath
          LEFT JOIN asset_type_inheritance cath ON ath.parent_asset_type_id = cath.child_asset_type_id
      WHERE
          ath.parent_asset_type_id = :parentAssetTypeId and
          ath.deleted_flag = false
  
      UNION
  
      SELECT child_asset_type_id, parent_asset_type_id
      FROM tree
          INNER JOIN asset_type_inheritance ath on tree.child = ath.parent_asset_type_id
      WHERE
          ath.deleted_flag = false
    )
    Select * From asset_type where asset_type_id in (SELECT child FROM tree);
  """, nativeQuery = true)
  List<AssetType> findAllAssetTypesByParentAssetTypeId (
    @Param("parentAssetTypeId") UUID parentAssetTypeId
  );

  @Query(value= """
    SELECT at
    FROM AssetType at
    Where
      at.assetTypeId in :assetTypeIds and
      at.isDeleted = false
  """)
  List<AssetType> findAssetTypeByIds (
    @Param("assetTypeIds") List<UUID> assetTypeIds
  );
}
