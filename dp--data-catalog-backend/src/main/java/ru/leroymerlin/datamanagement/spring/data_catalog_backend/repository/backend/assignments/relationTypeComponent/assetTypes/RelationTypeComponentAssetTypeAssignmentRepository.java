package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.AssetTypeRelationTypeComponentAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAssetTypeAssignmentAssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAssetTypeAssignmentWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponentAssetTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationTypeComponentAsset;

/**
 * @author juliwolf
 */

public interface RelationTypeComponentAssetTypeAssignmentRepository extends JpaRepository<RelationTypeComponentAssetTypeAssignment, UUID> {
  @Query(value = """
    SELECT rtcata FROM RelationTypeComponentAssetTypeAssignment rtcata
      left join fetch rtcata.relationTypeComponent rtc
      left join fetch rtcata.assetType at
      left join fetch rtcata.createdBy cb
      left join fetch rtcata.parentAssetType pat
    WHERE rtcata.relationTypeComponentAssetTypeAssignmentId = :relationTypeComponentAssetTypeAssignmentId
  """)
  Optional<RelationTypeComponentAssetTypeAssignment> findByIdWithJoinedTables (
    @Param("relationTypeComponentAssetTypeAssignmentId") UUID relationTypeComponentAssetTypeAssignmentId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAssetTypeAssignmentWithConnectedValues(
      at.assetTypeId, at.assetTypeName,
      rtcata.relationTypeComponentAssetTypeAssignmentId,
      rtcata.isInherited,
      pat.assetTypeId, pat.assetTypeName,
      rtcata.createdOn, rtcata.createdBy.userId
    )
    FROM RelationTypeComponentAssetTypeAssignment rtcata
      left join rtcata.relationTypeComponent rtc
      left join rtcata.assetType at
      left join rtcata.createdBy cb
      left join rtcata.parentAssetType pat
    WHERE rtcata.relationTypeComponent.relationTypeComponentId = :relationTypeComponentId
      and rtcata.isDeleted = false
    order by at.assetTypeName
  """)
  List<RelationTypeComponentAssetTypeAssignmentWithConnectedValues> findAllWithJoinedTablesByRelationTypeComponentId (
    @Param("relationTypeComponentId") UUID relationTypeComponentId
  );

  @Query(value= """
    with afd as (
      select asset_type_id, relation_type_component_id
      from relation_type_component_asset_type_assignment rtcata
      where rtcata.relation_type_component_asset_type_assignment_id = :relationTypeComponentAssetTypeAssignmentId
    )

    select *
    from relation_type_component_asset_type_assignment rtcata
    where
      rtcata.relation_type_component_id = (select relation_type_component_id from afd) and
      rtcata.parent_asset_type_id = (select asset_type_id from afd) and
      deleted_flag = false
  """, nativeQuery = true)
  List<RelationTypeComponentAssetTypeAssignment> findAllChildAssignmentsByRelationTypeComponentAssetTypeAssignmentId (
    @Param("relationTypeComponentAssetTypeAssignmentId") UUID relationTypeComponentAssetTypeAssignmentId
  );


  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationTypeComponentAsset(
      rtcata.relationTypeComponent.relationTypeComponentId, a.assetId
    )
    FROM RelationTypeComponentAssetTypeAssignment rtcata
    INNER JOIN AssetType at on at.assetTypeId = rtcata.assetType.assetTypeId
    INNER JOIN Asset a on a.assetType.assetTypeId = at.assetTypeId
    WHERE
      a.assetId in :assetIds and
      rtcata.relationTypeComponent.relationTypeComponentId in :relationTypeComponentIds and
      a.isDeleted = false and
      rtcata.isDeleted = false
  """)
  Set<RelationTypeComponentAsset> findAllByRelationTypeComponentAndAsset (
    @Param("assetIds") List<UUID> assetIds,
    @Param("relationTypeComponentIds") List<UUID> relationTypeComponentIds
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.AssetTypeRelationTypeComponentAssignment(
      rtcata.relationTypeComponentAssetTypeAssignmentId,
      rtcata.isInherited,
      rtcata.parentAssetType.assetTypeId, pat.assetTypeName,
      rtcata.relationTypeComponent.relationTypeComponentId, rtc.relationTypeComponentName, rtc.relationTypeComponentDescription,
      rtc.responsibilityInheritanceRole, rtc.hierarchyRole,
      rtc.relationType.relationTypeId, rt.relationTypeName, rt.relationTypeDescription,
      rt.componentNumber, rt.responsibilityInheritanceFlag, rt.hierarchyFlag, rt.uniquenessFlag,
      rtcata.createdOn, rtcata.createdBy.userId
    )
    FROM RelationTypeComponentAssetTypeAssignment rtcata
    Left Join AssetType pat on pat.assetTypeId = rtcata.parentAssetType.assetTypeId
    Left Join RelationTypeComponent rtc on rtc.relationTypeComponentId = rtcata.relationTypeComponent.relationTypeComponentId
    Left Join RelationType rt on rt.relationTypeId = rtc.relationType.relationTypeId
    Where
      rtcata.assetType.assetTypeId = :assetTypeId and
      (:hierarchyRole is null or rtc.hierarchyRole = :hierarchyRole) and
      (:responsibilityInheritanceRole is null or rtc.responsibilityInheritanceRole = :responsibilityInheritanceRole) and
      (:relationTypeComponentName is null or lower(rtc.relationTypeComponentName) LIKE '%' || lower(:relationTypeComponentName) || '%') and
      rtcata.isDeleted = false
  """, countQuery = """
    Select count(*)
    FROM RelationTypeComponentAssetTypeAssignment rtcata
    Left Join RelationTypeComponent rtc on rtc.relationTypeComponentId = rtcata.relationTypeComponent.relationTypeComponentId
    Where
      rtcata.assetType.assetTypeId = :assetTypeId and
      (:hierarchyRole is null or rtc.hierarchyRole = :hierarchyRole) and
      (:responsibilityInheritanceRole is null or rtc.responsibilityInheritanceRole = :responsibilityInheritanceRole) and
      (:relationTypeComponentName is null or lower(rtc.relationTypeComponentName) LIKE '%' || lower(:relationTypeComponentName) || '%') and
      rtcata.isDeleted = false
  """)
  Page<AssetTypeRelationTypeComponentAssignment> findAllByParamsPageable(
    @Param("assetTypeId") UUID assetTypeId,
    @Param("hierarchyRole") HierarchyRole hierarchyRole,
    @Param("responsibilityInheritanceRole") ResponsibilityInheritanceRole responsibilityInheritanceRole,
    @Param("relationTypeComponentName") String relationTypeComponentName,
    Pageable pageable
  );

  @Query(value = """
    SELECT rtcata
    FROM RelationTypeComponentAssetTypeAssignment rtcata
    WHERE
      rtcata.assetType.assetTypeId = :assetTypeId and
      rtcata.isDeleted = false
  """)
  List<RelationTypeComponentAssetTypeAssignment> findAllByAssetTypeId (
    @Param("assetTypeId") UUID assetTypeId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAssetTypeAssignmentAssetType(
       rtcata.relationTypeComponent.relationTypeComponentId,
       rtcata.relationTypeComponentAssetTypeAssignmentId, rtcata.assetType.assetTypeId
    )
    FROM RelationTypeComponentAssetTypeAssignment rtcata
    WHERE
      rtcata.relationTypeComponent.relationTypeComponentId in :relationTypeComponentIds and
      rtcata.isDeleted = false
  """)
  List<RelationTypeComponentAssetTypeAssignmentAssetType> findAllByRelationTypeComponentIds (
    @Param("relationTypeComponentIds") List<UUID> relationTypeComponentIds
  );

  @Modifying
  @Query(value = """
    UPDATE relation_type_component_asset_type_assignment
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      relation_type_component_id in :relationTypeComponentIds
  """, nativeQuery = true)
  void deleteAllByRelationTypeComponentIds (
    @Param("relationTypeComponentIds") List<UUID> relationTypeComponentIds,
    @Param("userId") UUID userId
  );

  @Modifying
  @Query(value = """
    UPDATE relation_type_component_asset_type_assignment
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      asset_type_id = :assetTypeId
  """, nativeQuery = true)
  void deleteAllByAssetTypeIds (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("userId") UUID userId
  );
}
