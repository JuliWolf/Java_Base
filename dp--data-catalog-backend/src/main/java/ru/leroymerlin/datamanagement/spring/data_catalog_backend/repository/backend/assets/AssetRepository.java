package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;

/**
 * @author JuliWolf
 */
public interface AssetRepository extends JpaRepository<Asset, UUID> {
  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetWithConnectedValues(
      a.assetId, a.assetName, a.assetDisplayName,
      at.assetTypeId, at.assetTypeName,
      ls.statusId, ls.statusName,
      ss.statusId, ss.statusName,
      l.language, a.createdOn, cb.userId,
      a.lastModifiedOn, mb.userId,
      a.isDeleted
    )
    FROM Asset a
    left join a.assetType at
    left join a.stewardshipStatus ss
    left join a.lifecycleStatus ls
    left join a.language l
    left join a.createdBy cb
    left join a.modifiedBy mb
    WHERE a.assetId = :assetId
  """)
  AssetWithConnectedValues getAssetByAssetIdWithJoinedTables (
    @Param("assetId") UUID assetId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetWithConnectedValues(
      a.assetId, a.assetName, a.assetDisplayName,
      at.assetTypeId, at.assetTypeName,
      ls.statusId, ls.statusName,
      ss.statusId, ss.statusName,
      l.language, a.createdOn, cb.userId,
      a.lastModifiedOn, mb.userId,
      a.isDeleted
    )
      FROM Asset a
      left join AssetHierarchy ah on ah.childAsset.assetId = a.assetId and ah.isDeleted = false
      left join a.assetType at
      left join a.stewardshipStatus ss
      left join a.lifecycleStatus ls
      left join a.language l
      left join a.createdBy cb
      left join a.modifiedBy mb
    WHERE
      (
        case
          when :isSearchAny = false
           then (
            (
              :assetName is null or
              case
                when :searchMode = 'ANY' THEN lower(a.assetName) LIKE '%' || lower(:assetName) || '%'
                when :searchMode = 'EXACT_MATCH' THEN a.assetName = :assetName
                when :searchMode = 'RIGHT' THEN lower(a.assetName) LIKE '%' || lower(:assetName)
                when :searchMode = 'LEFT' THEN lower(a.assetName) LIKE lower(:assetName) || '%'
              end
            ) and
            (
              :assetDisplayName is null or
              case
                when :searchMode = 'ANY' THEN lower(a.assetDisplayName) LIKE '%' || lower(:assetDisplayName) || '%'
                when :searchMode = 'EXACT_MATCH' THEN a.assetDisplayName = :assetDisplayName
                when :searchMode = 'RIGHT' THEN lower(a.assetDisplayName) LIKE '%' || lower(:assetDisplayName)
                when :searchMode = 'LEFT' THEN lower(a.assetDisplayName) LIKE lower(:assetDisplayName) || '%'
              end
            )
          )
          when :isSearchAny = true
            then (
              (
              :assetName is null or
              case
                when :searchMode = 'ANY' THEN lower(a.assetName) LIKE '%' || lower(:assetName) || '%'
                when :searchMode = 'EXACT_MATCH' THEN a.assetName = :assetName
                when :searchMode = 'RIGHT' THEN lower(a.assetName) LIKE '%' || lower(:assetName)
                when :searchMode = 'LEFT' THEN lower(a.assetName) LIKE lower(:assetName) || '%'
              end
            ) or
            (
              :assetDisplayName is null or
              case
                when :searchMode = 'ANY' THEN lower(a.assetDisplayName) LIKE '%' || lower(:assetDisplayName) || '%'
                when :searchMode = 'EXACT_MATCH' THEN a.assetDisplayName = :assetDisplayName
                when :searchMode = 'RIGHT' THEN lower(a.assetDisplayName) LIKE '%' || lower(:assetDisplayName)
                when :searchMode = 'LEFT' THEN lower(a.assetDisplayName) LIKE lower(:assetDisplayName) || '%'
              end
            )
          )
        end
      ) and
      (:rootFlag = false or (:rootFlag = true and ah.assetHierarchyId is null)) and
      (:assetTypeIdsCount = 0 OR a.assetType.assetTypeId in :assetTypeIds) and
      (:lifecycleStatusesCount = 0 OR a.lifecycleStatus.statusId in :lifecycleStatuses) and
      (:stewardshipStatusesCount = 0 OR a.stewardshipStatus.statusId in :stewardshipStatuses) and
      a.isDeleted = false
  """, countQuery = """
    SELECT count(a.assetId) FROM Asset a
    left join AssetHierarchy ah on ah.childAsset.assetId = a.assetId and ah.isDeleted = false
    WHERE
      (
        case
          when :isSearchAny = false
           then (
            (
              :assetName is null or
              case
                when :searchMode = 'ANY' THEN lower(a.assetName) LIKE '%' || lower(:assetName) || '%'
                when :searchMode = 'EXACT_MATCH' THEN a.assetName = :assetName
                when :searchMode = 'RIGHT' THEN lower(a.assetName) LIKE '%' || lower(:assetName)
                when :searchMode = 'LEFT' THEN lower(a.assetName) LIKE lower(:assetName) || '%'
              end
            ) and
            (
              :assetDisplayName is null or
              case
                when :searchMode = 'ANY' THEN lower(a.assetDisplayName) LIKE '%' || lower(:assetDisplayName) || '%'
                when :searchMode = 'EXACT_MATCH' THEN a.assetDisplayName = :assetDisplayName
                when :searchMode = 'RIGHT' THEN lower(a.assetDisplayName) LIKE '%' || lower(:assetDisplayName)
                when :searchMode = 'LEFT' THEN lower(a.assetDisplayName) LIKE lower(:assetDisplayName) || '%'
              end
            )
          )
          when :isSearchAny = true
            then (
              (
              :assetName is null or
              case
                when :searchMode = 'ANY' THEN lower(a.assetName) LIKE '%' || lower(:assetName) || '%'
                when :searchMode = 'EXACT_MATCH' THEN a.assetName = :assetName
                when :searchMode = 'RIGHT' THEN lower(a.assetName) LIKE '%' || lower(:assetName)
                when :searchMode = 'LEFT' THEN lower(a.assetName) LIKE lower(:assetName) || '%'
              end
            ) or
            (
              :assetDisplayName is null or
              case
                when :searchMode = 'ANY' THEN lower(a.assetDisplayName) LIKE '%' || lower(:assetDisplayName) || '%'
                when :searchMode = 'EXACT_MATCH' THEN a.assetDisplayName = :assetDisplayName
                when :searchMode = 'RIGHT' THEN lower(a.assetDisplayName) LIKE '%' || lower(:assetDisplayName)
                when :searchMode = 'LEFT' THEN lower(a.assetDisplayName) LIKE lower(:assetDisplayName) || '%'
              end
            )
          )
        end
      ) and
      (:rootFlag = false or (:rootFlag = true and ah.assetHierarchyId is null)) and
      (:assetTypeIdsCount = 0 OR a.assetType.assetTypeId in :assetTypeIds) and
      (:lifecycleStatusesCount = 0 OR a.lifecycleStatus.statusId in :lifecycleStatuses) and
      (:stewardshipStatusesCount = 0 OR a.stewardshipStatus.statusId in :stewardshipStatuses) and
      a.isDeleted = false
  """)
  Page<AssetWithConnectedValues> findAllByParamsWithJoinedTablesPageable (
    @Param("assetName") String assetName,
    @Param("assetDisplayName") String assetDisplayName,
    @Param("searchMode") String searchMode,
    @Param("isSearchAny") Boolean isSearchAny,
    @Param("rootFlag") Boolean rootFlag,
    @Param("assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param("assetTypeIds") List<UUID> assetTypeIds,
    @Param("lifecycleStatusesCount") Integer lifecycleStatusesCount,
    @Param("lifecycleStatuses") List<UUID> lifecycleStatuses,
    @Param("stewardshipStatusesCount") Integer stewardshipStatusesCount,
    @Param("stewardshipStatuses") List<UUID> stewardshipStatuses,
    Pageable pageable
  );

  @Query(value = """
    SELECT CAST(a.asset_id as text) as assetIdText FROM "asset" a
    WHERE
      (cast(:assetTypeId as uuid) is null or a.asset_type_id = :assetTypeId) and
      (
        cast(:statusId as uuid) is null or
        (
          a.lifecycle_status = :statusId or
          a.stewardship_status = :statusId
        )
      ) and
      a.deleted_flag = false
      LIMIT 1
  """, nativeQuery = true)
  List<AssetIdResponse> findFirstAssetByAssetTypeAndStatusId (
    @Param("statusId") UUID statusId,
    @Param("assetTypeId") UUID assetTypeId
  );

  @Query(value = """
    Select distinct
      cast(ast.asset_id as text) as assetIdText,
      cast(ast.asset_name as text) as name,
      cast(ast.asset_displayname as text) as displayname,
      cast(ast.asset_type_id as text) as assetTypeText,
      cast(at.asset_type_name as text) as assetTypeName,
      cast(swts.status_id as text) as stewardshipStatusIdText,
      cast(swts.status_name as text) as stewardshipStatusName,
      cast(lfts.status_id as text) as lifecycleStatusIdText,
      cast(lfts.status_name as text) as lifecycleStatusName,
      cast(atr.value as text) as description,
      ash.count as childrenCount,
      atr.attribute_id,
      atr.attribute_type_id
    From asset ast
    INNER JOIN asset_hierarchy ah on ast.asset_id = ah.child_asset_id
    INNER JOIN asset_type at on ast.asset_type_id = at.asset_type_id
    LEFT JOIN status swts on swts.status_id = ast.stewardship_status
    LEFT JOIN status lfts on lfts.status_id = ast.lifecycle_status
    LEFT JOIN attribute atr on atr.asset_id = ast.asset_id and
              atr.deleted_flag = false and
              (atr.attribute_type_id is null or atr.attribute_type_id = '00000000-0000-0000-0000-000000003114')
    LEFT JOIN (
      Select count(*) as count, parent_asset_id
      From asset_hierarchy
      where
        deleted_flag = false
      GROUP BY parent_asset_id
    ) ash on ash.parent_asset_id = ast.asset_id
    WHERE
      ah.parent_asset_id = :assetId and
      (:assetDisplayName is null or lower(ast.asset_displayname) LIKE '%' || lower(:assetDisplayName) || '%') and
      (:assetTypeIdsCount = 0 or ast.asset_type_id in (:assetTypeIds)) and
      (:lifecycleStatusIdsCount = 0 or ast.lifecycle_status in (:lifecycleStatusIds)) and
      (:stewardshipStatusIdsCount = 0 or ast.stewardship_status in (:stewardshipStatusIds)) and
      ast.deleted_flag = false and
      ah.deleted_flag = false
  """, countQuery = """
    Select count(dist.asset_id)
    From (
      Select distinct ast.asset_id, atr.attribute_id, atr.attribute_type_id
      From asset ast
      INNER JOIN asset_hierarchy ah on ast.asset_id = ah.child_asset_id
      INNER JOIN asset_type at on ast.asset_type_id = at.asset_type_id
      LEFT JOIN attribute atr on atr.asset_id = ast.asset_id and
                atr.deleted_flag = false and
                (atr.attribute_type_id is null or atr.attribute_type_id = '00000000-0000-0000-0000-000000003114')
      LEFT JOIN (
        Select count(*) as count, parent_asset_id
        From asset_hierarchy
        where
          deleted_flag = false
        GROUP BY parent_asset_id
      ) ash on ash.parent_asset_id = ast.asset_id
      WHERE
        ah.parent_asset_id = :assetId and
        (:assetDisplayName is null or lower(ast.asset_displayname) LIKE '%' || lower(:assetDisplayName) || '%') and
        (:assetTypeIdsCount = 0 or ast.asset_type_id in (:assetTypeIds)) and
        (:lifecycleStatusIdsCount = 0 or ast.lifecycle_status in (:lifecycleStatusIds)) and
        (:stewardshipStatusIdsCount = 0 or ast.stewardship_status in (:stewardshipStatusIds)) and
        ast.deleted_flag = false and
        ah.deleted_flag = false
    ) dist
  """, nativeQuery = true)
  Page<AssetChildren> getAssetsChildren (
    @Param("assetId") UUID assetId,
    @Param("assetDisplayName") String assetDisplayName,
    @Param("assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param("assetTypeIds") List<UUID> assetTypeIds,
    @Param("lifecycleStatusIdsCount") Integer lifecycleStatusIdsCount,
    @Param("lifecycleStatusIds") List<UUID> lifecycleStatusIds,
    @Param("stewardshipStatusIdsCount") Integer stewardshipStatusIdsCount,
    @Param("stewardshipStatusIds") List<UUID> stewardshipStatusIds,
    Pageable pageable
  );

  @Query(value = """
    WITH RECURSIVE chain(from_id, to_id) AS (
      SELECT NULL, cast(:assetId as text)

      UNION

      SELECT c.to_id, cast(ah.parent_asset_id as text)
      FROM chain c
      LEFT JOIN asset_hierarchy ah ON (cast(ah.child_asset_id as text) = to_id and not ah.deleted_flag)
      WHERE c.to_id IS NOT NULL
    )
    SELECT
      cast(asset_name as text) as assetName,
      cast(asset_displayname as text) as assetDisplayName,
      cast(path_elem_id as text) as assetIdText
    FROM asset a
    INNER JOIN (
      SELECT row_number() over () as num, cast(from_id as uuid) as path_elem_id
      FROM chain
      WHERE from_id is not null
    ) path_elems on path_elems.path_elem_id = a.asset_id
    order by path_elems.num
  """, nativeQuery = true)
  List<AssetHierarchyPathElement> getAssetHierarchyPathElements (
    @Param("assetId") UUID assetId
  );

  @Query(value = """
    WITH RECURSIVE chain(from_id, to_id) AS (
      SELECT NULL, cast(:assetId as text)

      UNION

      SELECT c.to_id, cast(ah.parent_asset_id as text)
      FROM chain c
      LEFT JOIN asset_hierarchy ah ON (cast(ah.child_asset_id as text) = to_id and not ah.deleted_flag)
      WHERE c.to_id IS NOT NULL
    )
    select STRING_AGG(asset_displayname, '>') textPath
    from (
        select asset_displayname, path_elem_id
        from asset a
        inner join (
          SELECT row_number() over () as num, cast(from_id as uuid) as path_elem_id
          FROM chain
          WHERE from_id is not null
        ) path_elems on path_elems.path_elem_id = a.asset_id
    order by path_elems.num desc
    ) fin
  """, nativeQuery = true)
  String getAssetHierarchyPath (
    @Param("assetId") UUID assetId
  );

  @Query(value = """
    SELECT cast(rt.relation_type_id as text) as relationTypeIdText, rt.relation_type_name as relationTypeName, rt1.total as total
    From relation_component rc
    INNER JOIN Relation r on rc.relation_id = r.relation_id
    INNER JOIN relation_type rt on r.relation_type_id = rt.relation_type_id
    INNER JOIN (
      select relation_type_id, count(*) as total
      from relation r1
      inner join (
        select relation_id, count(*)
        from public.relation_component rc
        where
          rc.asset_id = :assetId and
          rc.deleted_flag = false
        group by relation_id
      ) r on r1.relation_id = r.relation_id
      group by relation_type_id
    ) rt1 on rt1.relation_type_id = rt.relation_type_id
    WHERE
      rc.asset_id = :assetId and
      rc.deleted_flag = false
    Group by rt.relation_type_id, rt1.total
  """, nativeQuery = true)
  List<AssetRelationType> getAssetRelationType (
    @Param("assetId") UUID assetId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetRelationTypeComponent(
      rt.relationTypeId,
      rtc.relationTypeComponentId, rtc.relationTypeComponentName,
      count(*)
    )
    From RelationComponent rc
    INNER JOIN Relation r on rc.relation.relationId = r.relationId
    INNER JOIN RelationType rt on r.relationType.relationTypeId = rt.relationTypeId
    INNER JOIN RelationTypeComponent rtc on rc.relationTypeComponent.relationTypeComponentId = rtc.relationTypeComponentId
    WHERE
        rc.asset.assetId = :assetId and
        rc.isDeleted = false
    Group by rt.relationTypeId, rtc.relationTypeComponentId
  """)
  List<AssetRelationTypeComponent> getAssetRelationTypeComponents (
    @Param("assetId") UUID assetId
  );

  @Query("""
    Select a
    From Asset a
    Where
      a.assetId in :assetIds and
      a.isDeleted = false
  """)
  List<Asset> findAllByAssetIds(
    @Param("assetIds") List<UUID> assetIds
  );

  @Query("""
    Select a.assetId
    From Asset a
    Where
      a.assetId in :assetIds and
      a.isDeleted = false
  """)
  List<UUID> findIdsByAssetIds (
    @Param("assetIds") List<UUID> assetIds
  );

  @Modifying
  @Query(value ="""
    UPDATE asset
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE
      asset_id in :assetIds
  """, nativeQuery = true)
  void deleteAllByAssetIds (
    @Param("assetIds") List<UUID> assetIds,
    @Param("userId") UUID userId
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetAttributeLinkUsage(
      a.assetId, a.assetName, a.assetDisplayName,
      ast.assetTypeId, ast.assetTypeName,
      stws.statusId, stws.statusName,
      lfcs.statusId, lfcs.statusName,
      at.attributeTypeId, at.attributeTypeName,
      attr.value
    ) From AssetLinkUsage alu
      Inner join Attribute attr on alu.attribute.attributeId = attr.attributeId
      Inner join Asset a on attr.asset.assetId = a.assetId
      Left join AssetType ast on a.assetType.assetTypeId = ast.assetTypeId
      Left join AttributeType at on attr.attributeType.attributeTypeId = at.attributeTypeId
      Left join Status stws on a.stewardshipStatus.statusId = stws.statusId
      Left join Status lfcs on a.lifecycleStatus.statusId = lfcs.statusId
      WHERE
        (:assetTypeIdsCount = 0 OR ast.assetTypeId in :assetTypeIds) and
        (:attributeTypeIdsCount = 0 OR at.attributeTypeId in :attributeTypeIds) and
        (:lifecycleStatusIdsCount = 0 OR lfcs.statusId in :lifecycleStatusIds) and
        (:stewardshipStatusIdsCount = 0 OR stws.statusId in :stewardshipStatusIds) and
        alu.isDeleted = false and
        a.isDeleted = false and
        alu.asset.assetId = :assetId
  """, countQuery = """
    Select count(alu.assetLinkUsageId)
      From AssetLinkUsage alu
      Inner join Attribute attr on alu.attribute.attributeId = attr.attributeId
      Inner join Asset a on attr.asset.assetId = a.assetId
      Left join AssetType ast on a.assetType.assetTypeId = ast.assetTypeId
      Left join AttributeType at on attr.attributeType.attributeTypeId = at.attributeTypeId
      Left join Status stws on a.stewardshipStatus.statusId = stws.statusId
      Left join Status lfcs on a.lifecycleStatus.statusId = lfcs.statusId
      WHERE
        (:assetTypeIdsCount = 0 OR ast.assetTypeId in :assetTypeIds) and
        (:attributeTypeIdsCount = 0 OR at.attributeTypeId in :attributeTypeIds) and
        (:lifecycleStatusIdsCount = 0 OR lfcs.statusId in :lifecycleStatusIds) and
        (:stewardshipStatusIdsCount = 0 OR stws.statusId in :stewardshipStatusIds) and
        alu.isDeleted = false and
        a.isDeleted = false and
        alu.asset.assetId = :assetId
  """)
  Page<AssetAttributeLinkUsage> findAllAssetAttributeLinkUsageByAssetIdPageable (
    @Param("assetId") UUID assetId,
    @Param("assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param("assetTypeIds") List<UUID> assetTypeIds,
    @Param("attributeTypeIdsCount") Integer attributeTypeIdsCount,
    @Param("attributeTypeIds") List<UUID> attributeTypeIds,
    @Param("lifecycleStatusIdsCount") Integer lifecycleStatusIdsCount,
    @Param("lifecycleStatusIds") List<UUID> lifecycleStatusIds,
    @Param("stewardshipStatusIdsCount") Integer stewardshipStatusIdsCount,
    @Param("stewardshipStatusIds") List<UUID> stewardshipStatusIds,
    Pageable pageable
  );


  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetWithDescription(
      a.assetId, a.assetName, a.assetDisplayName,
      at.assetTypeId, at.assetTypeName,
      lfs.statusId, lfs.statusName,
      stws.statusId, stws.statusName,
      att.value,
      (count(cv.customViewId) > 0), l.language,
      a.createdOn, a.createdBy.userId,
      a.lastModifiedOn, a.modifiedBy.userId
    )
    From Asset a
    Inner Join AssetType at on at.assetTypeId = a.assetType.assetTypeId
    Left Join Status stws on stws.statusId = a.stewardshipStatus.statusId
    Left Join Status lfs on lfs.statusId = a.lifecycleStatus.statusId
    Left Join AssetTypeCardHeaderAssignment atcha on atcha.assetType.assetTypeId = at.assetTypeId and atcha.isDeleted = false
    Left Join AttributeType atrt on atrt.attributeTypeId = atcha.descriptionFieldAttributeType.attributeTypeId
    Left Join Attribute att on att.attributeType.attributeTypeId = coalesce(atrt.attributeTypeId, :staticAttributeTypeId) and att.asset.assetId = :assetId and att.isDeleted = false
    Left join CustomView cv on cv.assetType.assetTypeId = at.assetTypeId and cv.isDeleted = false
    Left Join Language l on l.languageId = a.language.languageId
    where
      a.assetId = :assetId and
      a.isDeleted = false
    group by a.assetId, at.assetTypeId, stws.statusId, lfs.statusId, atcha.assetTypeCardHeaderAssignmentId, atrt.attributeTypeId, att.attributeId, l.languageId
  """)
  Optional<AssetWithDescription> findAssetWithDetails(
    @Param("assetId") UUID assetId,
    @Param("staticAttributeTypeId") UUID staticAttributeTypeId
  );

  @Query(value = """
    select reportAt.value as reportId,
      a.asset_displayname as reportName, a.stewardship_status as assetStewardshipStatusId, a.asset_id as reportDataCatalogId,
      descAt.value as reportDescription, linkAt.value as reportLink, templateAt.value as reportTemplate, statusAt.value as reviewStatus,
      confidentialityAt.value as reportConfidentiality, trainingMatAt.value as reportTrainingMaterials, uniquUsersAt.value_numeric as uniqueUsersLastMonth,
      lastModifiedAt.value_datetime as reportLastModifiedDate, u.username as technicalOwner, reviewRequestat.value as reportReviewRequestId
    from Asset a
    left join Attribute reportAt on a.asset_id = reportAt.asset_id and reportAt.deleted_flag = false and reportAt.attribute_type_id = 'cd8a00b0-6ad2-47b8-9021-d1a89d78122f'
    left join Attribute descAt on a.asset_id = descAt.asset_id and descAt.deleted_flag = false and descAt.attribute_type_id = '00000000-0000-0000-0000-000000003114'
    left join Attribute linkAt on a.asset_id = linkAt.asset_id and linkAt.deleted_flag = false and linkAt.attribute_type_id = '00000000-0000-0000-0000-000000000203'
    left join Attribute templateAt on a.asset_id = templateAt.asset_id and templateAt.deleted_flag = false and templateAt.attribute_type_id = '944e1b50-d175-4561-ac1d-2ed417339664'
    left join Attribute statusAt on a.asset_id = statusAt.asset_id and statusAt.deleted_flag = false and statusAt.attribute_type_id = '00000000-0000-0000-0001-000500000001'
    left join Attribute confidentialityAt on a.asset_id = confidentialityAt.asset_id and confidentialityAt.deleted_flag = false and confidentialityAt.attribute_type_id = 'eb633e06-cdd3-43b4-bdcb-9a31859211fe'
    left join Attribute trainingMatAt on a.asset_id = trainingMatAt.asset_id and trainingMatAt.deleted_flag = false and trainingMatAt.attribute_type_id = '2ba18c15-4218-46fa-96a0-da1cd1d0ada1'
    left join Attribute uniquUsersAt on a.asset_id = uniquUsersAt.asset_id and uniquUsersAt.deleted_flag = false and uniquUsersAt.attribute_type_id = 'bcf8bfec-6240-4d04-a280-ba6b42d2006c'
    left join Attribute lastModifiedAt on a.asset_id = lastModifiedAt.asset_id and lastModifiedAt.deleted_flag = false and lastModifiedAt.attribute_type_id = '00000000-0000-0000-0000-000000000255'
    left join Attribute reviewRequestat on a.asset_id = reviewRequestat.asset_id and reviewRequestat.deleted_flag = false and reviewRequestat.attribute_type_id = '20e752bb-5e3c-45b6-9141-979be8aa874d'
    left join Responsibility resp on a.asset_id = resp.asset_id and resp.deleted_flag = false and resp.role_id = '00000000-0000-0000-0000-000000005038'
    left join "user" u on resp.user_id = u.user_id
    where
      (:reviewStatus is null or statusAt.value = :reviewStatus) and
      (:hasDataCatalogDescription is null or case
        when :hasDataCatalogDescription = true then a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74'
        when :hasDataCatalogDescription = false then a.stewardship_status != '55a86990-84bd-4c95-af43-eb015224ba74'
        end
      ) and
      (:reportName is null or a.asset_displayname = :reportName) and
      (:uniqueUsersLastMonthMin is null or uniquUsersAt.value_numeric >= :uniqueUsersLastMonthMin) and
      (:uniqueUsersLastMonthMax is null or uniquUsersAt.value_numeric <= :uniqueUsersLastMonthMax) and
      (lastModifiedAt.value_datetime >= :reportLastModifiedDateMin and lastModifiedAt.value_datetime <= :reportLastModifiedDateMax) and
      (:reportConfidentiality is null or confidentialityAt.value = :reportConfidentiality) and
      a.deleted_flag = false and
      a.asset_type_id = '00000000-0000-0000-0000-090000000003' and
      a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
      a.stewardship_status != '5269e364-ab34-4fb9-942f-f51703f827ce'
  """, countQuery = """
    select count(reportAt.value)
    from Asset a
    left join Attribute reportAt on a.asset_id = reportAt.asset_id and reportAt.deleted_flag = false and reportAt.attribute_type_id = 'cd8a00b0-6ad2-47b8-9021-d1a89d78122f'
    left join Attribute descAt on a.asset_id = descAt.asset_id and descAt.deleted_flag = false and descAt.attribute_type_id = '00000000-0000-0000-0000-000000003114'
    left join Attribute linkAt on a.asset_id = linkAt.asset_id and linkAt.deleted_flag = false and linkAt.attribute_type_id = '00000000-0000-0000-0000-000000000203'
    left join Attribute templateAt on a.asset_id = templateAt.asset_id and templateAt.deleted_flag = false and templateAt.attribute_type_id = '944e1b50-d175-4561-ac1d-2ed417339664'
    left join Attribute statusAt on a.asset_id = statusAt.asset_id and statusAt.deleted_flag = false and statusAt.attribute_type_id = '00000000-0000-0000-0001-000500000001'
    left join Attribute confidentialityAt on a.asset_id = confidentialityAt.asset_id and confidentialityAt.deleted_flag = false and confidentialityAt.attribute_type_id = 'eb633e06-cdd3-43b4-bdcb-9a31859211fe'
    left join Attribute trainingMatAt on a.asset_id = trainingMatAt.asset_id and trainingMatAt.deleted_flag = false and trainingMatAt.attribute_type_id = '2ba18c15-4218-46fa-96a0-da1cd1d0ada1'
    left join Attribute uniquUsersAt on a.asset_id = uniquUsersAt.asset_id and uniquUsersAt.deleted_flag = false and uniquUsersAt.attribute_type_id = 'bcf8bfec-6240-4d04-a280-ba6b42d2006c'
    left join Attribute lastModifiedAt on a.asset_id = lastModifiedAt.asset_id and lastModifiedAt.deleted_flag = false and lastModifiedAt.attribute_type_id = '00000000-0000-0000-0000-000000000255'
    left join Attribute reviewRequestat on a.asset_id = reviewRequestat.asset_id and reviewRequestat.deleted_flag = false and reviewRequestat.attribute_type_id = '20e752bb-5e3c-45b6-9141-979be8aa874d'
    left join Responsibility resp on a.asset_id = resp.asset_id and resp.deleted_flag = false and resp.role_id = '00000000-0000-0000-0000-000000005038'
    left join "user" u on resp.user_id = u.user_id
    where
      (:reviewStatus is null or statusAt.value = :reviewStatus) and
      (:hasDataCatalogDescription is null or case
        when :hasDataCatalogDescription = true then a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74'
        when :hasDataCatalogDescription = false then a.stewardship_status != '55a86990-84bd-4c95-af43-eb015224ba74'
        end
      ) and
      (:reportName is null or a.asset_displayname = :reportName) and
      (:uniqueUsersLastMonthMin is null or uniquUsersAt.value_numeric >= :uniqueUsersLastMonthMin) and
      (:uniqueUsersLastMonthMax is null or uniquUsersAt.value_numeric <= :uniqueUsersLastMonthMax) and
      (lastModifiedAt.value_datetime >= :reportLastModifiedDateMin and lastModifiedAt.value_datetime <= :reportLastModifiedDateMax) and
      (:reportConfidentiality is null or confidentialityAt.value = :reportConfidentiality) and
      a.deleted_flag = false and
      a.asset_type_id = '00000000-0000-0000-0000-090000000003' and
      a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
      a.stewardship_status != '5269e364-ab34-4fb9-942f-f51703f827ce'
  """, nativeQuery = true)
  Page<AssetReport> getReportsByParamsPageable (
    @Param("reviewStatus") String reviewStatus,
    @Param("hasDataCatalogDescription") Boolean hasDataCatalogDescription,
    @Param("reportName") String reportName,
    @Param("uniqueUsersLastMonthMin") Long uniqueUsersLastMonthMin,
    @Param("uniqueUsersLastMonthMax") Long uniqueUsersLastMonthMax,
    @Param("reportLastModifiedDateMin") java.sql.Timestamp reportLastModifiedDateMin,
    @Param("reportLastModifiedDateMax") java.sql.Timestamp reportLastModifiedDateMax,
    @Param("reportConfidentiality") String reportConfidentiality,
    Pageable pageable
  );

  @Query(value = """
    select reportAt.value as reportId,
      a.asset_displayname as reportName, a.stewardship_status as assetStewardshipStatusId, a.asset_id as reportDataCatalogId,
      descAt.value as reportDescription, linkAt.value as reportLink, templateAt.value as reportTemplate, statusAt.value as reviewStatus,
      confidentialityAt.value as reportConfidentiality, trainingMatAt.value as reportTrainingMaterials, uniquUsersAt.value_numeric as uniqueUsersLastMonth,
      lastModifiedAt.value_datetime as reportLastModifiedDate, u.username as technicalOwner, reviewRequestat.value as reportReviewRequestId
    from Asset a
    left join Attribute reportAt on a.asset_id = reportAt.asset_id and reportAt.deleted_flag = false and reportAt.attribute_type_id = 'cd8a00b0-6ad2-47b8-9021-d1a89d78122f'
    left join Attribute descAt on a.asset_id = descAt.asset_id and descAt.deleted_flag = false and descAt.attribute_type_id = '00000000-0000-0000-0000-000000003114'
    left join Attribute linkAt on a.asset_id = linkAt.asset_id and linkAt.deleted_flag = false and linkAt.attribute_type_id = '00000000-0000-0000-0000-000000000203'
    left join Attribute templateAt on a.asset_id = templateAt.asset_id and templateAt.deleted_flag = false and templateAt.attribute_type_id = '944e1b50-d175-4561-ac1d-2ed417339664'
    left join Attribute statusAt on a.asset_id = statusAt.asset_id and statusAt.deleted_flag = false and statusAt.attribute_type_id = '00000000-0000-0000-0001-000500000001'
    left join Attribute confidentialityAt on a.asset_id = confidentialityAt.asset_id and confidentialityAt.deleted_flag = false and confidentialityAt.attribute_type_id = 'eb633e06-cdd3-43b4-bdcb-9a31859211fe'
    left join Attribute trainingMatAt on a.asset_id = trainingMatAt.asset_id and trainingMatAt.deleted_flag = false and trainingMatAt.attribute_type_id = '2ba18c15-4218-46fa-96a0-da1cd1d0ada1'
    left join Attribute uniquUsersAt on a.asset_id = uniquUsersAt.asset_id and uniquUsersAt.deleted_flag = false and uniquUsersAt.attribute_type_id = 'bcf8bfec-6240-4d04-a280-ba6b42d2006c'
    left join Attribute lastModifiedAt on a.asset_id = lastModifiedAt.asset_id and lastModifiedAt.deleted_flag = false and lastModifiedAt.attribute_type_id = '00000000-0000-0000-0000-000000000255'
    left join Attribute reviewRequestat on a.asset_id = reviewRequestat.asset_id and reviewRequestat.deleted_flag = false and reviewRequestat.attribute_type_id = '20e752bb-5e3c-45b6-9141-979be8aa874d'
    left join Responsibility resp on a.asset_id = resp.asset_id and resp.deleted_flag = false and resp.role_id = '00000000-0000-0000-0000-000000005038'
    left join "user" u on resp.user_id = u.user_id
    where
      a.asset_id = :reportId and
      a.deleted_flag = false and
      a.asset_type_id = '00000000-0000-0000-0000-090000000003' and
      a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
      a.stewardship_status != '5269e364-ab34-4fb9-942f-f51703f827ce'
  """, nativeQuery = true)
  Optional<AssetReport> getReportsById (
    @Param("reportId") UUID reportId
  );

  @Query(value = """
    Select a.*
    From Asset a
    Where
      a.asset_id = :reportId and
      a.deleted_flag = false and
      a.asset_type_id = '00000000-0000-0000-0000-090000000003' and
      a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
      a.stewardship_status != '5269e364-ab34-4fb9-942f-f51703f827ce'
  """, nativeQuery = true)
  Optional<Asset> findAssetByReportId (
    @Param("reportId") UUID reportId
  );

  @Query(value = """
    Select count(a.asset_id) > 0
    From Asset a
    Where
      a.asset_id = :reportId and
      a.deleted_flag = false and
      a.asset_type_id = '00000000-0000-0000-0000-090000000003' and
      a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
      a.stewardship_status != '5269e364-ab34-4fb9-942f-f51703f827ce'
  """, nativeQuery = true)
  Boolean isReportExistById (
    @Param("reportId") UUID reportId
  );

  @Modifying
  @Query(value = """
    update attribute
    set value = :reviewStatus
    Where
      deleted_flag = false and
      attribute_type_id = '00000000-0000-0000-0001-000500000001' and
      asset_id = :reportId
  """, nativeQuery = true)
  void updateReportReviewStatus (
    @Param("reportId") UUID reportId,
    @Param("reviewStatus") String reviewStatus
  );
}
