package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.models.AssetTypeStatusCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.models.AssetTypeStatusAssignmentWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeStatusAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;

/**
 * @author JuliWolf
 */
public interface AssetTypeStatusAssignmentRepository extends JpaRepository<AssetTypeStatusAssignment, UUID> {
  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.models.AssetTypeStatusAssignmentWithConnectedValues(
      ast.assetTypeId, ast.assetTypeName,
      atsa.assetTypeStatusAssignmentId,
      s.statusId, s.statusName, s.statusDescription,
      atsa.isInherited,
      past.assetTypeId, past.assetTypeName,
      atsa.assignmentStatusType,
      atsa.createdOn, atsa.createdBy.userId
    )
    FROM AssetTypeStatusAssignment atsa
      left join Status s on s.statusId = atsa.status.statusId
      left join AssetType ast on ast.assetTypeId = atsa.assetType.assetTypeId
      left join AssetType past on past.assetTypeId = atsa.parentAssetType.assetTypeId
    WHERE
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null or atsa.assetType.assetTypeId = :assetTypeId) and
      (:statusType is null or atsa.assignmentStatusType = :statusType) and
      atsa.isDeleted = false
    order by s.statusName
  """)
  List<AssetTypeStatusAssignmentWithConnectedValues> findAllByAssignmentIdAndStatusTypePageable (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("statusType") AssignmentStatusType statusType
  );

  @Query(value = """
    SELECT atsa FROM AssetTypeStatusAssignment atsa
      left join fetch atsa.status s
      left join fetch atsa.assetType ast
      left join fetch atsa.createdBy cb
    WHERE
      (cast(:statusId as org.hibernate.type.PostgresUUIDType) is null or atsa.status.statusId = :statusId) and
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null or atsa.assetType.assetTypeId = :assetTypeId) and
      (:statusType is null or atsa.assignmentStatusType = :statusType) and
      atsa.isDeleted = false
  """, countQuery = """
  """)
  Page<AssetTypeStatusAssignment> findAllByParamsWithJoinedTablesPageable (
    @Param("statusId") UUID statusId,
    @Param("statusType") AssignmentStatusType statusType,
    @Param("assetTypeId") UUID assetTypeId,
    Pageable pageable
  );

  @Query(value = """
    SELECT atsa
    FROM AssetTypeStatusAssignment atsa
      left join fetch atsa.assetType at
      left join fetch atsa.status s
      left join fetch atsa.createdBy cb
    WHERE
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null or atsa.assetType.assetTypeId = :assetTypeId) and
      atsa.isDeleted = false
  """)
  List<AssetTypeStatusAssignment> findAllByAssetTypeIdWithJoinedTables (
    @Param("assetTypeId") UUID assetTypeId
  );

  @Modifying
  @Query(value = """
    UPDATE asset_type_status_assignment
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      asset_type_id = :assetTypeId
  """, nativeQuery = true)
  void deleteByAssetTypeId (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("userId") UUID userId
  );

  @Query(value = """
    SELECT count(asset_type_status_assignment_id) > 0
    FROM "asset_type_status_assignment" atsa
    WHERE
      (cast(:statusId as uuid) is null or atsa.status_id = :statusId) and
      ((:assignmentStatusType = '') is null or atsa.status_type = :assignmentStatusType) and
      atsa.deleted_flag = false
      FETCH FIRST 1 ROWS ONLY
  """, nativeQuery = true)
  Boolean isExistsAssetTypeStatusAssignmentsByStatusIdAndAssignmentStatusType (
    @Param("statusId") UUID statusId,
    @Param("assignmentStatusType") String assignmentStatusType
  );

  @Query(value = """
    SELECT atsa.status.statusId
    FROM AssetTypeStatusAssignment atsa
    WHERE
      (atsa.status.statusId in :statusIds) and
      atsa.assignmentStatusType = :assignmentStatusType and
      atsa.isDeleted = false
  """)
  Set<UUID> getAssetTypeStatusAssignmentsByStatusIdAndAssignmentStatusType (
    @Param("statusIds") List<UUID> statusIds,
    @Param("assignmentStatusType") AssignmentStatusType assignmentStatusType
  );

  @Query(value= """
    with afd as
      (select asset_type_id, status_id from asset_type_status_assignment asa
        where asa.asset_type_status_assignment_id = :assetTypeStatusAssignmentId)

    select * from asset_type_status_assignment asa
    where
      asa.status_id = (select status_id from afd) and
      asa.parent_asset_type_id = (select asset_type_id from afd) and
      deleted_flag = false
  """, nativeQuery = true)
  List<AssetTypeStatusAssignment> findAllChildAssignmentsByAssetTypeStatusAssignmentId (
    @Param("assetTypeStatusAssignmentId") UUID assetTypeStatusAssignmentId
  );

  @Query("""
     select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.models.AssetTypeStatusCount(
        atsa.assetTypeStatusAssignmentId,
        count(*)
     )
     from Asset a
     inner join AssetTypeStatusAssignment atsa on atsa.assetType.assetTypeId = a.assetType.assetTypeId and atsa.status.statusId = a.lifecycleStatus.statusId
     where
         not a.isDeleted and
         atsa.assetTypeStatusAssignmentId in :assetTypeStatusAssignmentIds
     group by atsa.assetTypeStatusAssignmentId, a.lifecycleStatus.statusId

     union
  
     select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.models.AssetTypeStatusCount(
        atsa.assetTypeStatusAssignmentId,
        count(*)
     )
     from Asset a
     inner join AssetTypeStatusAssignment atsa on atsa.assetType.assetTypeId = a.assetType.assetTypeId and atsa.status.statusId = a.stewardshipStatus.statusId
     where
         not a.isDeleted and
         atsa.assetTypeStatusAssignmentId in :assetTypeStatusAssignmentIds
     group by atsa.assetTypeStatusAssignmentId, a.stewardshipStatus.statusId
  """)
  List<AssetTypeStatusCount> countAssetTypeStatusUsage (
    @Param("assetTypeStatusAssignmentIds") List<UUID> assetTypeStatusAssignmentIds
  );
}
