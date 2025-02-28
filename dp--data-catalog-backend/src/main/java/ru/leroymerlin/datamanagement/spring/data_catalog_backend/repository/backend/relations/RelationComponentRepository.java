package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.*;

/**
 * @author juliwolf
 */

public interface RelationComponentRepository extends JpaRepository<RelationComponent, UUID> {
  @Query(value = """
    WITH RECURSIVE tree(parent, child, relationId) AS (
      select
        crc.asset_id as parentAsset,
        rc.asset_id as childAsset,
        rc.relation_id as relationId
      from relation_component rc
      inner join relation_component crc on
        rc.relation_id = crc.relation_id and
        crc.responsibility_inheritance_role = 'SOURCE' and
        crc.deleted_flag = false
      where
        rc.responsibility_inheritance_role = 'CONSUMER' and
        rc.deleted_flag = false and
        rc.relation_id in (
          select crc.relation_id
          from relation_component crc
          where
            crc.asset_id in :assetIds and
            crc.responsibility_inheritance_role = 'SOURCE' and
            crc.deleted_flag = false
        )

      UNION

      SELECT prc.asset_id, crc.asset_id, crc.relation_id
      FROM tree
      inner join relation_component prc on
        prc.responsibility_inheritance_role = 'SOURCE' and
        prc.asset_id = child and
        prc.deleted_flag = false
      inner join relation_component crc on
        prc.relation_id = crc.relation_id and
        crc.responsibility_inheritance_role = 'CONSUMER' and
        crc.deleted_flag = false
    )
    Select
      cast(tree.child as text) as consumerAssetIdText,
      cast(tree.relationId as text) as relationIdText,
      cast(tree.parent as text) as sourceAssetIdText,
      sub.count as consumersCount
    from tree
    Left Join (
        Select parent, count(*) as count
        From tree as tr
        Group By tr.parent
    ) sub on sub.parent = tree.child
  """, nativeQuery = true)
  List<SourceAssetRelationComponent> findAllRelationComponentHierarchyByAssetIdsPageable (
    @Param("assetIds") Set<UUID> assetIds,
    Pageable pageable
  );

  @Query(value = """
    WITH RECURSIVE tree(parent, child, relationId) AS (
      select
        crc.asset_id as parentAsset,
        rc.asset_id as childAsset,
        rc.relation_id as relationId
      from relation_component rc
      inner join relation_component crc on
        rc.relation_id = crc.relation_id and
        crc.responsibility_inheritance_role = 'SOURCE' and
        crc.deleted_flag = false
      where
        rc.responsibility_inheritance_role = 'CONSUMER' and
        rc.deleted_flag = false and
        rc.relation_id in (
          select crc.relation_id
          from relation_component crc
          where
            crc.asset_id in :assetIds and
            crc.responsibility_inheritance_role = 'SOURCE' and
            crc.deleted_flag = false
        )

      UNION

      SELECT prc.asset_id, crc.asset_id, crc.relation_id
      FROM tree
      inner join relation_component prc on
        prc.responsibility_inheritance_role = 'SOURCE' and
        prc.asset_id = child and
        prc.deleted_flag = false
      inner join relation_component crc on
        prc.relation_id = crc.relation_id and
        crc.responsibility_inheritance_role = 'CONSUMER' and
        crc.deleted_flag = false
    )
    Select count(*)
    from tree;
  """, nativeQuery = true)
  Long countRelationComponentHierarchyByAssetIds (
    @Param("assetIds") Set<UUID> assetIds
  );

  @Query(value = """
    select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithRelationComponentAndAsset(
      r.relationId, r.relationType.relationTypeId,
      rc.relationComponentId, rc.relationTypeComponent.relationTypeComponentId, rc.asset.assetId
    )
    from RelationComponent rc
    Inner join Relation r on r.relationId = rc.relation.relationId
    where
        rc.relation.relationId in :relationIds and
        rc.isDeleted = false
  """)
  List<RelationWithRelationComponentAndAsset> findAllByRelationIds (
    @Param("relationIds") List<UUID> relationIds
  );

  @Modifying
  @Query(value = """
    UPDATE relation_component
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

  @Query(value = """
    select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationComponentWithConnectedValues(
      rc.relation.relationId,
      rc.relationComponentId, a.assetId, a.assetDisplayName, a.assetName,
      at.assetTypeId, at.assetTypeName,
      stws.statusId, stws.statusName,
      lfs.statusId, lfs.statusName,
      rc.hierarchyRole, rc.responsibilityInheritanceRole,
      rtc.relationTypeComponentId, rtc.relationTypeComponentName,
      rc.createdOn, rc.createdBy.userId
    )
    from RelationComponent rc
    left join Asset a on a.assetId = rc.asset.assetId
    left join AssetType at on at.assetTypeId = a.assetType.assetTypeId
    left join Status stws on stws.statusId = a.stewardshipStatus.statusId
    left join Status lfs on lfs.statusId = a.lifecycleStatus.statusId
    left join RelationTypeComponent rtc on rc.relationTypeComponent.relationTypeComponentId = rtc.relationTypeComponentId
    where
        rc.relation.relationId in :relationIds and
        rc.isDeleted = false
  """)
  List<RelationComponentWithConnectedValues> findAllByRelationIdsWithConnectedValues (
    @Param("relationIds") List<UUID> relationIds
  );

  @Query(value = """
    select count(rc.relationTypeComponent.relationTypeComponentId) > 0
    from RelationComponent rc
    INNER JOIN Asset a on rc.asset.assetId = a.assetId
    INNER JOIN RelationTypeComponentAssetTypeAssignment rtcata on a.assetType.assetTypeId = rtcata.assetType.assetTypeId and rc.relationTypeComponent.relationTypeComponentId = rtcata.relationTypeComponent.relationTypeComponentId
    where
        rtcata.assetType.assetTypeId = :assetTypeId and
        rtcata.relationTypeComponent.relationTypeComponentId = :relationTypeComponentId and
        rc.isDeleted = false
  """)
  Boolean isRelationsExistsByAssetId (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("relationTypeComponentId") UUID relationTypeComponentId
  );

  @Query(value = """
    select rc from RelationComponent rc
    Inner join rc.relation r
    where
        r.relationType.relationTypeId = :relationTypeId and
        rc.isDeleted = false and
        r.isDeleted = false
  """)
  List<RelationComponent> findAllRelationComponentsByRelationTypeId (
    @Param("relationTypeId") UUID relationTypeId
  );

  @Query(value= """
    WITH RECURSIVE asset_responsibility_hierarchy(relation_id, consumer_asset_id, search_asset_id) AS (
        SELECT parentRelComp.relation_id, parentRelComp.asset_id as consumer_asset_id, rel.asset_id as search_asset_id
        FROM relation_component parentRelComp
        LEFT JOIN relation_component rel ON parentRelComp.relation_id = rel.relation_id
        WHERE
            parentRelComp.asset_id = :assetId and
            rel.asset_id <> parentRelComp.asset_id and
            parentRelComp.deleted_flag = false and
            parentRelComp.responsibility_inheritance_role = 'SOURCE' and
            rel.deleted_flag = false
        UNION

        SELECT childRelComp.relation_id, parentRelComp.asset_id as source_asset_id, childRelComp.asset_id as consumer_asset_id
        FROM relation_component childRelComp
        LEFT JOIN relation_component parentRelComp ON childRelComp.relation_id = parentRelComp.relation_id
        INNER JOIN asset_responsibility_hierarchy arh on arh.search_asset_id = parentRelComp.asset_id
        WHERE
            parentRelComp.asset_id <> childRelComp.asset_id and
            childRelComp.responsibility_inheritance_role = 'CONSUMER'
    )
    SELECT distinct on (rc.asset_id) rc.* FROM relation_component rc
    Where
        asset_id in (Select search_asset_id from asset_responsibility_hierarchy) and
        rc.deleted_flag = false;
  """, nativeQuery = true)
  List<RelationComponent> findAllResponsibilityInheritanceRoleHierarchyByAssetId (
    @Param("assetId") UUID assetId
  );

  @Query("""
    Select (count(distinct leftComp.relationComponentId) + count(distinct rightComp.relationComponentId)) as count
    From RelationComponent leftComp
    INNER JOIN RelationComponent rightComp ON
        rightComp.relation.relationId = leftComp.relation.relationId and
        leftComp.relationComponentId <> rightComp.relationComponentId
    INNER JOIN Relation rel on rel.relationId = leftComp.relation.relationId
    WHERE
        leftComp.relationComponentId <> rightComp.relationComponentId and
        rel.relationType.relationTypeId = :relationTypeId and
        leftComp.relationTypeComponent.relationTypeComponentId = :leftRelationTypeComponentId and
        leftComp.asset.assetId = :leftAssetId and
        rightComp.relationTypeComponent.relationTypeComponentId in :rightRelationTypeComponentIds and
        rightComp.asset.assetId in :rightAssetIds and
        leftComp.isDeleted = false
  """)
  Integer countExistingRelationComponents (
    @Param("relationTypeId") UUID relationTypeId,
    @Param("leftAssetId") UUID leftAssetId,
    @Param("leftRelationTypeComponentId") UUID leftRelationTypeComponentId,
    @Param("rightAssetIds") List<UUID> rightAssetIds,
    @Param("rightRelationTypeComponentIds") List<UUID> rightRelationTypeComponentIds
  );

  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithRelationComponentAndAsset(
      rel.relationId, rel.relationType.relationTypeId,
      relComp.relationComponentId, relComp.relationTypeComponent.relationTypeComponentId, relComp.asset.assetId
    )
    From RelationComponent relComp
    INNER JOIN Relation rel on rel.relationId = relComp.relation.relationId
    WHERE
        rel.relationType.relationTypeId in :relationTypeIds and
        relComp.asset.assetId in :assetIds and
        relComp.relationTypeComponent.relationTypeComponentId in :relationTypeComponentIds and
        relComp.isDeleted = false
    ORDER by relComp.relationComponentId
  """)
  List<RelationWithRelationComponentAndAsset> findAllByRelationTypesAsseIdsRelationTypeComponentIds (
    @Param("relationTypeIds") List<UUID> relationTypeIds,
    @Param("assetIds") List<UUID> assetIds,
    @Param("relationTypeComponentIds") List<UUID> relationTypeComponentIds
  );

  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationComponentWithRelationComponentAttributes(
      rc.relationComponentId, a.assetId, a.assetDisplayName,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType, at.validationMask, STRING_AGG(atav.value, ';'),
      rca.relationComponentAttributeId, rca.value,
      rc.hierarchyRole, rc.responsibilityInheritanceRole,
      rtc.relationTypeComponentId, rtc.relationTypeComponentName,
      rc.createdOn, rc.createdBy.userId
    ) from RelationComponent rc
    inner join RelationTypeComponent rtc on rtc.relationTypeComponentId = rc.relationTypeComponent.relationTypeComponentId
    left join Asset a on a.assetId = rc.asset.assetId
    left join RelationTypeComponentAttributeTypeAssignment rtcata on rtcata.relationTypeComponent.relationTypeComponentId = rtc.relationTypeComponentId and rtcata.isDeleted = false
    left join AttributeType at on at.attributeTypeId = rtcata.attributeType.attributeTypeId and at.isDeleted = false
    left join AttributeTypeAllowedValue atav on atav.attributeType.attributeTypeId = at.attributeTypeId and atav.isDeleted = false
    left join RelationComponentAttribute rca on rca.attributeType.attributeTypeId = at.attributeTypeId and rca.relationComponent.relationComponentId = rc.relationComponentId and rca.isDeleted = false
    Where
      rc.relation.relationId = :relationId and
      rc.isDeleted = false
    Group by rc.relationComponentId, a.assetId, at.attributeTypeId, rca.relationComponentAttributeId, rtc.relationTypeComponentId, rtcata.attributeType.attributeTypeId
  """)
  List<RelationComponentWithRelationComponentAttributes> findAllRelationComponentAttributeByRelationIds (
    @Param("relationId") UUID relationId
  );

  @Query("""
    select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationComponentAssetCount(
      rc.relation.relationId, rc.asset.assetId
    )
    from RelationComponent rc
    inner join Relation r on rc.relation.relationId = r.relationId and r.relationType.relationTypeId = :relationTypeId and not r.isDeleted
    group by rc.relation.relationId, rc.asset.assetId
    having count(rc.relation.relationId) > 1
  """)
  List<RelationComponentAssetCount> countRelationComponentAssetByRelationType (
    @Param("relationTypeId") UUID relationTypeId
  );

  @Query("""
    select rc.asset.assetId
    from RelationComponent rc
    inner join RelationComponent rc2 on
       rc.relation.relationId = rc2.relation.relationId and
       rc.asset.assetId in :assetIds and
       not rc.isDeleted and
       rc.responsibilityInheritanceRole = 'SOURCE'
  """)
  List<UUID> findAllSourceAssetIdsByAssetIds (
    @Param("assetIds") List<UUID> assetIds
  );

  @Query(value = """
    Select count(*)
    From (
      select rc.relation_type_component_id as relationTypeComponentId, rc.asset_id
      from relation_component rc
      Where
            rc.deleted_flag = false and
            rc.relation_type_component_id = :relationTypeComponentId
      group by rc.relation_type_component_id, rc.asset_id
      having count(*) > 1
    ) s
  """, nativeQuery = true)
  Integer countRelationTypeComponentAssets (
    @Param("relationTypeComponentId") UUID relationTypeComponentId
  );

  @Query(value = """
      select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationTypeComponentAsset(
        rc.relationTypeComponent.relationTypeComponentId,
        rc.asset.assetId
      )
      from RelationComponent rc
      Where
            rc.isDeleted = false and
            rc.asset.assetId in :assetIds and
            rc.relationTypeComponent.relationTypeComponentId in :relationTypeComponentIds
      group by rc.relationTypeComponent.relationTypeComponentId, rc.asset.assetId
      having count(*) >= 1
  """)
  List<RelationTypeComponentAsset> countRelationTypeComponentAssetsByComponentAndAssetIds (
    @Param("assetIds") List<UUID> assetIds,
    @Param("relationTypeComponentIds") List<UUID> relationTypeComponentIds
  );
}
