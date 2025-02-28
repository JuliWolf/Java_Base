package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Relation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationAssetId;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithAttributes;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithRelationComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithRelationType;

/**
 * @author juliwolf
 */

public interface RelationRepository extends JpaRepository<Relation, UUID> {
  @Query(value= """
    select distinct new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithRelationComponent(
      r.relationId, rt.relationTypeId, rt.relationTypeName, rt.responsibilityInheritanceFlag, rt.hierarchyFlag, rt.uniquenessFlag,
      r.createdOn, r.createdBy.userId,
      rc.relationComponentId, a.assetId, a.assetDisplayName, a.assetName,
      at.assetTypeId, at.assetTypeName,
      stws.statusId, stws.statusName,
      lfs.statusId, lfs.statusName,
      rc.hierarchyRole, rc.responsibilityInheritanceRole,
      rtc.relationTypeComponentId, rtc.relationTypeComponentName,
      rc.createdOn, rc.createdBy.userId
    )
    from Relation r
    inner join RelationComponent rc on rc.relation.relationId = r.relationId
    inner join RelationType rt on r.relationType.relationTypeId = rt.relationTypeId
    left join Asset a on a.assetId = rc.asset.assetId
    left join AssetType at on at.assetTypeId = a.assetType.assetTypeId
    left join Status stws on stws.statusId = a.stewardshipStatus.statusId
    left join Status lfs on lfs.statusId = a.lifecycleStatus.statusId
    left join RelationTypeComponent rtc on rc.relationTypeComponent.relationTypeComponentId = rtc.relationTypeComponentId
    WHERE
      r.relationId = :relationId and
      r.isDeleted = false and
      rc.isDeleted = false
    Order by rtc.relationTypeComponentName
  """)
  List<RelationWithRelationComponent> findByIdWithConnectedValues (
    @Param("relationId") UUID relationId
  );

  @Query(value= """
    SELECT count(r.relationId)
    FROM Relation r
    WHERE
      r.relationId in :relationIds and
      r.isDeleted = false
  """)
  Integer countIdsByRelationIds (
    @Param("relationIds") List<UUID> relationIds
  );

  @Modifying
  @Query(value ="""
    UPDATE relation
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

  @Query(value= """
    SELECT r
    FROM Relation r
    left join r.relationComponents rc
    WHERE
      rc.asset.assetId in :assetIds and
      rc.isDeleted = false and
      r.isDeleted = false
  """)
  List<Relation> findAllByAssetIds (
    @Param("assetIds") List<UUID> assetIds
  );

  @Query(value= """
    SELECT r.relationId
    FROM Relation r
    left join r.relationComponents rc
    WHERE
      rc.asset.assetId in :assetIds and
      rc.isDeleted = false and
      r.isDeleted = false
  """)
  Set<UUID> findAllRelationIdByAssetIds (
    @Param("assetIds") List<UUID> assetIds
  );

  @Query(value = """
    select distinct new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithRelationType(
      r.relationId, rt.relationTypeId, rt.relationTypeName, rt.responsibilityInheritanceFlag, rt.hierarchyFlag, rt.uniquenessFlag,
      r.createdOn, r.createdBy.userId
    )
    from Relation r
    inner join RelationComponent rc on rc.relation.relationId = r.relationId
    inner join RelationType rt on r.relationType.relationTypeId = rt.relationTypeId
    left join Asset a on a.assetId = rc.asset.assetId
    left join RelationTypeComponent rtc on rc.relationTypeComponent.relationTypeComponentId = rtc.relationTypeComponentId
    where
        (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null or rc.asset.assetId = :assetId) and
        (cast(:relationTypeId as org.hibernate.type.PostgresUUIDType) is null or rt.relationTypeId = :relationTypeId) and
        (cast(:relationTypeComponentId as org.hibernate.type.PostgresUUIDType) is null or rtc.relationTypeComponentId = :relationTypeComponentId) and
        (:hierarchyFlag is null
          or (:hierarchyFlag = true and rc.hierarchyRole is not null)
          or (:hierarchyFlag = false and rc.hierarchyRole is null)
        ) and
        (:responsibilityInheritanceFlag is null
          or (:responsibilityInheritanceFlag = true and rc.responsibilityInheritanceRole is not null)
          or (:responsibilityInheritanceFlag = false and rc.responsibilityInheritanceRole is null)
        ) and
        r.isDeleted = false and
        rc.isDeleted = false
  """, countQuery = """
    select count(distinct r.relationId)
    from Relation r
    inner join RelationComponent rc on rc.relation.relationId = r.relationId
    inner join RelationType rt on r.relationType.relationTypeId = rt.relationTypeId
    left join RelationTypeComponent rtc on rc.relationTypeComponent.relationTypeComponentId = rtc.relationTypeComponentId
    where
        (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null or rc.asset.assetId = :assetId) and
        (cast(:relationTypeId as org.hibernate.type.PostgresUUIDType) is null or rt.relationTypeId = :relationTypeId) and
        (cast(:relationTypeComponentId as org.hibernate.type.PostgresUUIDType) is null or rtc.relationTypeComponentId = :relationTypeComponentId) and
        (:hierarchyFlag is null
          or (:hierarchyFlag = true and rc.hierarchyRole is not null)
          or (:hierarchyFlag = false and rc.hierarchyRole is null)
        ) and
        (:responsibilityInheritanceFlag is null
          or (:responsibilityInheritanceFlag = true and rc.responsibilityInheritanceRole is not null)
          or (:responsibilityInheritanceFlag = false and rc.responsibilityInheritanceRole is null)
        ) and
        r.isDeleted = false and
        rc.isDeleted = false
  """)
  Page<RelationWithRelationType> findAllByParamsPageable (
    @Param("assetId") UUID assetId,
    @Param("relationTypeId") UUID relationTypeId,
    @Param("relationTypeComponentId") UUID relationTypeComponentId,
    @Param("hierarchyFlag") Boolean hierarchyFlag,
    @Param("responsibilityInheritanceFlag") Boolean responsibilityInheritanceFlag,
    Pageable pageable
  );

  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationAssetId(
      r.relationType.relationTypeId,
      rc.asset.assetId
    ) from Relation r
    inner join r.relationComponents rc on rc.relation.relationId = r.relationId
    Where r.relationId = :relationId
  """)
  List<RelationAssetId> findAllRelationAssetIds (
    @Param("relationId") UUID relationId
  );

  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithAttributes(
      r.relationId, rt.relationTypeId, rt.relationTypeName, rt.responsibilityInheritanceFlag, rt.hierarchyFlag,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType, at.validationMask, STRING_AGG(atav.value, ';'),
      ra.relationAttributeId, ra.value,
      r.createdOn, r.createdBy.userId
    ) from Relation r
    inner join RelationType rt on rt.relationTypeId = r.relationType.relationTypeId
    left join RelationTypeAttributeTypeAssignment rtata on rtata.relationType.relationTypeId = rt.relationTypeId and rtata.isDeleted = false
    left join AttributeType at on at.attributeTypeId = rtata.attributeType.attributeTypeId
    left join AttributeTypeAllowedValue atav on atav.attributeType.attributeTypeId = at.attributeTypeId and atav.isDeleted = false
    left join RelationAttribute ra on ra.attributeType.attributeTypeId = at.attributeTypeId and ra.relation.relationId = r.relationId and ra.isDeleted= false
    Where
      r.relationId = :relationId and
      r.isDeleted = false
    Group By r.relationId, rt.relationTypeId, rtata.relationTypeAttributeTypeAssignmentId, at.attributeTypeId, ra.relationAttributeId
  """)
  List<RelationWithAttributes> findAllRelationAttributesByRelationId(
    @Param("relationId") UUID relationId
  );

  @Query("""
    Select r
    From Relation r
    where
      r.relationId in :relationIds and
      r.isDeleted = false
  """)
  List<Relation> findAllByRelationIds (
    @Param("relationIds") List<UUID> relationIds
  );
}
