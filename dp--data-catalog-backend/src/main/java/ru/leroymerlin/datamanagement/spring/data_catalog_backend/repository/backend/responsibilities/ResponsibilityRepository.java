package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities;

import java.sql.Timestamp;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Responsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models.ResponsibilityToDelete;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models.ResponsibilityWithConnectedValues;

/**
 * @author JuliWolf
 */
public interface ResponsibilityRepository extends JpaRepository<Responsibility, UUID> {
  @Query(value = """
    SELECT resp FROM Responsibility resp
      left join fetch resp.asset a
      left join fetch resp.user u
      left join fetch resp.group g
      left join fetch resp.role r
      left join fetch resp.parentResponsibility pr
      left join fetch resp.createdBy cb
    WHERE resp.responsibilityId = :responsibilityId
  """)
  Optional<Responsibility> findByIdWithJoinedTables (
    @Param("responsibilityId") UUID responsibilityId
  );

  @Query(value = """
    SELECT resp
    FROM Responsibility resp
    WHERE
      (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null OR resp.asset.assetId = :assetId) and
      (cast(:roleId as org.hibernate.type.PostgresUUIDType) is null OR resp.role.roleId = :roleId) and
      (cast(:userId as org.hibernate.type.PostgresUUIDType) is null OR resp.user.userId = :userId) and
      (cast(:groupId as org.hibernate.type.PostgresUUIDType) is null OR resp.group.groupId = :groupId) and
      (cast(:parentResponsibilityId as org.hibernate.type.PostgresUUIDType) is null or (resp.parentResponsibility is not null and resp.parentResponsibility.responsibilityId = :parentResponsibilityId)) and
      (:inheritedFlag is null or resp.inheritedFlag = :inheritedFlag) and
      resp.isDeleted = false
  """)
  List<Responsibility> findAllByParams (
    @Param("assetId") UUID assetId,
    @Param("roleId") UUID roleId,
    @Param("userId") UUID userId,
    @Param("groupId") UUID groupId,
    @Param("parentResponsibilityId") UUID parentResponsibilityId,
    @Param("inheritedFlag") Boolean inheritedFlag
  );

  @Query(value = """
    SELECT resp
    FROM Responsibility resp
    WHERE
      resp.asset.assetId in :assetIds and
      resp.isDeleted = false
  """)
  List<Responsibility> findAllByAssetIds (
    @Param("assetIds") Set<UUID> assetIds
  );

  @Query(value = """
    SELECT resp
    FROM Responsibility resp
    WHERE
      resp.responsibilityId in :responsibilitiesIds and
      resp.isDeleted = false
  """)
  List<Responsibility> findAllByResponsibilitiesIds (
    @Param("responsibilitiesIds") List<UUID> responsibilitiesIds
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models.ResponsibilityWithConnectedValues(
      resp.responsibilityId, g.groupId, g.groupName,
      u.userId, u.username, concat(u.firstName, ' ' ,u.lastName),
      resp.responsibleType, r.roleId, r.roleName,
      a.assetId, a.assetDisplayName, a.assetName,
      at.assetTypeId, at.assetTypeName,
      stws.statusId, stws.statusName,
      lfs.statusId, lfs.statusName,
      resp.inheritedFlag, pr.responsibilityId,
      resp.relation.relationId, resp.createdOn, resp.createdBy.userId
    )
    FROM Responsibility resp
      left join Asset a on a.assetId = resp.asset.assetId
      left join AssetType at on at.assetTypeId = a.assetType.assetTypeId
      left join Status stws on stws.statusId = a.stewardshipStatus.statusId
      left join Status lfs on lfs.statusId = a.lifecycleStatus.statusId
      left join User u on u.userId = resp.user.userId
      left join Group g on g.groupId = resp.group.groupId
      left join Role r on r.roleId = resp.role.roleId
      left join Responsibility pr on pr.responsibilityId = resp.parentResponsibility.responsibilityId
    WHERE
      (:assetsIdsCount = 0 OR resp.asset.assetId in :assetIds) and
      (:rolesIdsCount = 0 OR resp.role.roleId in :roleIds) and
      (:usersIdsCount = 0 OR resp.user.userId in :userIds) and
      (:groupsIdsCount = 0 OR resp.group.groupId in :groupIds) and
      (:assetTypeIdsCount = 0 OR at.assetTypeId in :assetTypeIds) and
      (:lifecycleStatusIdsCount = 0 OR lfs.statusId in :lifecycleStatusIds) and
      (:stewardshipStatusIdsCount = 0 OR stws.statusId in :stewardshipStatusIds) and
      (:inheritedFlag is null or resp.inheritedFlag = :inheritedFlag) and
      resp.isDeleted = false
  """, countQuery = """
    SELECT count(resp)
    FROM Responsibility resp
    left join Asset a on a.assetId = resp.asset.assetId
    left join AssetType at on at.assetTypeId = a.assetType.assetTypeId
    left join Status stws on stws.statusId = a.stewardshipStatus.statusId
    left join Status lfs on lfs.statusId = a.lifecycleStatus.statusId
    WHERE
      (:assetsIdsCount = 0 OR resp.asset.assetId in :assetIds) and
      (:rolesIdsCount = 0 OR resp.role.roleId in :roleIds) and
      (:usersIdsCount = 0 OR resp.user.userId in :userIds) and
      (:groupsIdsCount = 0 OR resp.group.groupId in :groupIds) and
      (:assetTypeIdsCount = 0 OR at.assetTypeId in :assetTypeIds) and
      (:lifecycleStatusIdsCount = 0 OR lfs.statusId in :lifecycleStatusIds) and
      (:stewardshipStatusIdsCount = 0 OR stws.statusId in :stewardshipStatusIds) and
      (:inheritedFlag is null or resp.inheritedFlag = :inheritedFlag) and
      resp.isDeleted = false
  """)
  Page<ResponsibilityWithConnectedValues> findAllByParamsWithJoinedTablesPageable (
    @Param("assetsIdsCount") Integer assetsIdsCount,
    @Param("assetIds") List<UUID> assetIds,
    @Param("rolesIdsCount") Integer rolesIdsCount,
    @Param("roleIds") List<UUID> roleIds,
    @Param("usersIdsCount") Integer usersIdsCount,
    @Param("userIds") List<UUID> userIds,
    @Param("groupsIdsCount") Integer groupsIdsCount,
    @Param("groupIds") List<UUID> groupIds,
    @Param("assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param("assetTypeIds") List<UUID> assetTypeIds,
    @Param("lifecycleStatusIdsCount") Integer lifecycleStatusIdsCount,
    @Param("lifecycleStatusIds") List<UUID> lifecycleStatusIds,
    @Param("stewardshipStatusIdsCount") Integer stewardshipStatusIdsCount,
    @Param("stewardshipStatusIds") List<UUID> stewardshipStatusIds,
    @Param("inheritedFlag") Boolean inheritedFlag,
    Pageable pageable
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models.ResponsibilityWithConnectedValues(
      resp.responsibilityId, g.groupId, g.groupName,
      u.userId, u.username, concat(u.firstName, ' ' ,u.lastName),
      resp.responsibleType, r.roleId, r.roleName,
      a.assetId, a.assetDisplayName, a.assetName,
      at.assetTypeId, at.assetTypeName,
      stws.statusId, stws.statusName,
      lfs.statusId, lfs.statusName,
      resp.inheritedFlag, pr.responsibilityId,
      resp.relation.relationId, resp.createdOn, resp.createdBy.userId
    )
    FROM Responsibility resp
      left join Asset a on a.assetId = resp.asset.assetId
      left join AssetType at on at.assetTypeId = a.assetType.assetTypeId
      left join Status stws on stws.statusId = a.stewardshipStatus.statusId
      left join Status lfs on lfs.statusId = a.lifecycleStatus.statusId
      left join User u on u.userId = resp.user.userId
      left join Group g on g.groupId = resp.group.groupId
      left join Role r on r.roleId = resp.role.roleId
      left join Responsibility pr on pr.responsibilityId = resp.parentResponsibility.responsibilityId
    WHERE
      resp.responsibilityId = :responsibilityId and
      resp.isDeleted = false
  """)
  Optional<ResponsibilityWithConnectedValues> findResponsibilityById (
    @Param("responsibilityId") UUID responsibilityId
  );

  @Query(value = """
    SELECT resp FROM Responsibility resp
    WHERE
      resp.relation.relationId in :relationIds and
      resp.isDeleted = false
  """)
  List<Responsibility> findAllByRelationIds (
    @Param("relationIds") List<UUID> relationIds
  );

  @Query(value= """
    Select r
    from Responsibility r
    WHERE
      (r.user is null or cast(:userId as org.hibernate.type.PostgresUUIDType) is null or r.user.userId = :userId) and
      (r.group is null or cast(:groupId as org.hibernate.type.PostgresUUIDType) is null or r.group.groupId = :groupId) and
      (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null OR r.asset.assetId = :assetId) and
      (cast(:roleId as org.hibernate.type.PostgresUUIDType) is null OR r.role.roleId = :roleId) and
      (:responsibleType is null OR r.responsibleType = :responsibleType) and
      (cast(:relationId as org.hibernate.type.PostgresUUIDType) is null OR r.relation.relationId = :relationId) and
      r.isDeleted = false
  """)
  Optional<Responsibility> findResponsibilityByParams (
    @Param("userId") UUID userId,
    @Param("groupId") UUID groupId,
    @Param("assetId") UUID assetId,
    @Param("roleId") UUID roleId,
    @Param("responsibleType") ResponsibleType responsibleType,
    @Param("relationId") UUID relationId
  );

  @Query(
    """
      Select distinct new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models.ResponsibilityToDelete(
        resp.responsibilityId, resp.asset.assetId, resp.inheritedFlag, rc.relation.relationId
      )
      From Responsibility resp
      left join RelationComponent rc on
          rc.asset.assetId = resp.asset.assetId and
          rc.isDeleted = false and rc.responsibilityInheritanceRole = 'SOURCE'
      Where
        resp.responsibilityId in :responsibilitiesIds and
        resp.isDeleted = false
    """
  )
  List<ResponsibilityToDelete> findResponsibilitiesToDeleteByResponsibilitiesIds (
    @Param("responsibilitiesIds") List<UUID> responsibilitiesIds
  );

  @Modifying
  @Query(value = """
    UPDATE responsibility
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :deletedBy
    WHERE
      (cast(:assetId as uuid) is null OR asset_id = :assetId) and
      (cast(:roleId as uuid) is null OR role_id = :roleId) and
      (cast(:userId as uuid) is null OR user_id = :userId) and
      (cast(:groupId as uuid) is null OR group_id = :groupId) and
      (cast(:parentResponsibilityId as uuid) is null or (parent_responsibility_id is not null and parent_responsibility_id = :parentResponsibilityId)) and
      (:inheritedFlag is null or inherited_flag = :inheritedFlag)
  """, nativeQuery = true)
  void deleteAllByParams (
    @Param("assetId") UUID assetId,
    @Param("roleId") UUID roleId,
    @Param("userId") UUID userId,
    @Param("groupId") UUID groupId,
    @Param("parentResponsibilityId") UUID parentResponsibilityId,
    @Param("inheritedFlag") Boolean inheritedFlag,
    @Param("deletedBy") UUID deletedBy
  );

  @Modifying
  @Query(value = """
    UPDATE responsibility
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

  @Modifying
  @Query(value = """
    UPDATE responsibility
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE
      responsibility.responsibility_id in :responsibilitiesIds
  """, nativeQuery = true)
  void deleteAllByResponsibilityIds (
    @Param("responsibilitiesIds") List<UUID> responsibilitiesIds,
    @Param("userId") UUID userId
  );

  @Modifying
  @Query(value = """
    WITH RECURSIVE tree(child, parent) AS (
      SELECT resp.responsibility_id, resp.parent_responsibility_id
      FROM responsibility resp
      WHERE
        resp.relation_id in :relationIds and
        resp.deleted_flag = false

      UNION
      
      SELECT resp.responsibility_id, parent
      FROM tree
      INNER JOIN responsibility resp on tree.child = resp.parent_responsibility_id
      WHERE
        resp.deleted_flag = false
      )
      UPDATE responsibility
      Set
        deleted_flag = true,
        deleted_on = current_timestamp,
        deleted_by = :userId
      WHERE (
        responsibility_id in (SELECT child FROM tree) or
        relation_id in :relationIds
      ) and deleted_flag = false
  """, nativeQuery = true)
  void deleteAllByRelationIds (
    @Param("relationIds") List<UUID> relationIds,
    @Param("userId") UUID userId
  );

  @Modifying
  @Query(value = """
    WITH RECURSIVE tree(child, parent) AS (
     SELECT resp.responsibility_id, resp.parent_responsibility_id
     FROM responsibility resp
     WHERE
       resp.parent_responsibility_id in :parentResponsibilityIds and
       resp.deleted_flag = false
 
     UNION
 
     SELECT resp.responsibility_id, parent
     FROM tree
        INNER JOIN responsibility resp on tree.child = resp.parent_responsibility_id
     WHERE
       resp.deleted_flag = false
   )
   UPDATE responsibility
   Set
     deleted_flag = true,
     deleted_on = current_timestamp,
     deleted_by = :userId
   where
    responsibility_id in (SELECT child FROM tree);
  """, nativeQuery = true)
  void deleteAllByParentResponsibilityId (
    @Param("parentResponsibilityIds") List<UUID> parentResponsibilityIds,
    @Param("userId") UUID userId
  );

  @Query(
    """
      Select resp.responsibilityId
      From Responsibility resp
      Where
        resp.asset.assetId = :assetId and
        resp.createdOn = :createdOn and
        resp.isDeleted = false
    """
  )
  List<UUID> findParentResponsibilitiesByParams(
    @Param("assetId") UUID assetId,
    @Param("createdOn") Timestamp createdOn
  );

  @Modifying
  @Query(value = """
    INSERT INTO responsibility (responsibility_id, asset_id, created_by, created_on, relation_id, inherited_flag, parent_responsibility_id, user_id, group_id, role_id, responsible_type)
    Select
      :responsibilityId,
      :assetId,
      :createdBy,
      :createdOn,
      :relationId,
      true,
      :parentResponsibilityId,
      resp.user_id,
      resp.group_id,
      resp.role_id,
      resp.responsible_type
    From responsibility resp
    Where resp.responsibility_id = :parentResponsibilityId
  """, nativeQuery = true)
  void createResponsibilityFromParentResponsibility (
    @Param("responsibilityId") UUID responsibilityId,
    @Param("assetId") UUID assetId,
    @Param("relationId") UUID relationId,
    @Param("createdBy") UUID createdBy,
    @Param("createdOn") Timestamp createdOn,
    @Param("parentResponsibilityId") UUID parentResponsibilityId
  );
}
