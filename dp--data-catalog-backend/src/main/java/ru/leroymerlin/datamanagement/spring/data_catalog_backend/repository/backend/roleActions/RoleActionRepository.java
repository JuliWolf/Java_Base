package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions;

import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RoleAction;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;

import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService.CACHE_NAME;

/**
 * @author JuliWolf
 */
public interface RoleActionRepository extends JpaRepository<RoleAction, UUID>  {
  @Query(value = """
    SELECT rc
    FROM RoleAction rc
    WHERE
      rc.role.roleId = :roleId and
      rc.entity.id = :entityTypeId and
      rc.actionType.actionTypeId = :actionTypeId and
      (:permissionType is null or rc.permissionType = :permissionType) and
      rc.isDeleted = false
  """)
  List<RoleAction> findAllByRoleEntityAction (
    @Param("roleId") UUID roleId,
    @Param("entityTypeId") UUID entityTypeId,
    @Param("actionTypeId") UUID actionTypeId,
    @Param("permissionType") PermissionType permissionType
  );

  @Cacheable(value = CACHE_NAME)
  @Query(value= """
    SELECT
      CAST(ra.role_action_id as text) as roleActionIdText, CAST(ra.role_id as text) as roleIdText,
      e.entity_name as entityName, ra.permission_type as permissionType, ra.action_scope as actionScopeType,
      CAST(r.role_id as text) as roleTypeIdText, CAST(at.asset_type_id as text) as assetTypeIdText,
      CAST(att.attribute_type_id as text) as attributeTypeIdText, CAST(rt.relation_type_id as text) as relationTypeIdText,
      null as assetIdText
    FROM role_action ra
    INNER JOIN action_type act on act.action_type_id = ra.action_type_id
    INNER JOIN global_responsibility gresp on ra.role_id = gresp.role_id
    INNER JOIN entity e on ra.entity_type_id = e.entity_id
    LEFT JOIN role r on ra.role_type_id = r.role_id
    LEFT JOIN asset_type at on ra.asset_type_id = at.asset_type_id
    LEFT JOIN attribute_type att on ra.attribute_type_id = att.attribute_type_id
    LEFT JOIN relation_type rt on ra.relation_type_id = rt.relation_type_id
    WHERE
      gresp.user_id = :userId and
      gresp.responsible_type = 'USER' and
      (e.entity_name in :entityTypeNames) and
      (act.action_type_name in :actionTypeNames) and
      (:roleIdsCount = 0 or (ra.action_scope = 'ONE_ID' and r.role_id in (:roleIds)) or r.role_id is null) and
      (:assetTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and at.asset_type_id in (:assetTypeIds)) or at.asset_type_id is null) and
      (:attributeTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and att.attribute_type_id in (:attributeTypeIds)) or att.attribute_type_id is null) and
      (:relationTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and rt.relation_type_id in (:relationTypeIds)) or rt.relation_type_id is null) and
      ra.deleted_flag = false and
      gresp.deleted_flag = false
  """, nativeQuery = true)
  List<RoleActionResponse> findAllByUserIdActionTypesAndEntityName (
    @Param(value = "userId") UUID userId,
    @Param(value = "actionTypeNames") List<String> actionTypeNames,
    @Param(value = "entityTypeNames") List<String> entityTypeNames,
    @Param(value = "roleIdsCount") Integer roleIdsCount,
    @Param(value = "roleIds") List<UUID> roleIds,
    @Param(value = "assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param(value = "assetTypeIds") List<UUID> assetTypeIds,
    @Param(value = "attributeTypeIdsCount") Integer attributeTypeIdsCount,
    @Param(value = "attributeTypeIds") List<UUID> attributeTypeIds,
    @Param(value = "relationTypeIdsCount") Integer relationTypeIdsCount,
    @Param(value = "relationTypeIds") List<UUID> relationTypeIds
  );

  @Cacheable(value = CACHE_NAME)
  @Query(value= """
    SELECT
      CAST(ra.role_action_id as text) as roleActionIdText, CAST(ra.role_id as text) as roleIdText,
      e.entity_name as entityName, ra.permission_type as permissionType, ra.action_scope as actionScopeType,
      CAST(r.role_id as text) as roleTypeIdText, CAST(at.asset_type_id as text) as assetTypeIdText,
      CAST(att.attribute_type_id as text) as attributeTypeIdText, CAST(rt.relation_type_id as text) as relationTypeIdText,
      null as assetIdText
    FROM role_action ra
    INNER JOIN action_type act on act.action_type_id = ra.action_type_id
    INNER JOIN global_responsibility gresp on ra.role_id = gresp.role_id
    INNER JOIN entity e on ra.entity_type_id = e.entity_id
    LEFT JOIN role r on ra.role_type_id = r.role_id
    LEFT JOIN asset_type at on ra.asset_type_id = at.asset_type_id
    LEFT JOIN attribute_type att on ra.attribute_type_id = att.attribute_type_id
    LEFT JOIN relation_type rt on ra.relation_type_id = rt.relation_type_id
    WHERE
      gresp.responsible_type = 'GROUP' and
      gresp.group_id in (
        Select group_id
        FROM user_group ug
        WHERE
          ug.user_id = :userId and
          ug.deleted_flag = false
      ) and
      (act.action_type_name in :actionTypeNames) and
      e.entity_name in :entityTypeNames and
      (:roleIdsCount = 0 or (ra.action_scope = 'ONE_ID' and r.role_id in (:roleIds)) or r.role_id is null) and
      (:assetTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and at.asset_type_id in (:assetTypeIds)) or at.asset_type_id is null) and
      (:attributeTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and att.attribute_type_id in (:attributeTypeIds)) or att.attribute_type_id is null) and
      (:relationTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and rt.relation_type_id in (:relationTypeIds)) or rt.relation_type_id is null) and
      ra.deleted_flag = false and
      gresp.deleted_flag = false
  """, nativeQuery = true)
  List<RoleActionResponse> findAllByUserGroupsActionTypesAndEntityName (
    @Param(value = "userId") UUID userId,
    @Param(value = "actionTypeNames") List<String> actionTypeNames,
    @Param(value = "entityTypeNames") List<String> entityTypeNames,
    @Param(value = "roleIdsCount") Integer roleIdsCount,
    @Param(value = "roleIds") List<UUID> roleIds,
    @Param(value = "assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param(value = "assetTypeIds") List<UUID> assetTypeIds,
    @Param(value = "attributeTypeIdsCount") Integer attributeTypeIdsCount,
    @Param(value = "attributeTypeIds") List<UUID> attributeTypeIds,
    @Param(value = "relationTypeIdsCount") Integer relationTypeIdsCount,
    @Param(value = "relationTypeIds") List<UUID> relationTypeIds
  );

  @Cacheable(value = CACHE_NAME)
  @Query(value= """
    SELECT
      distinct CAST(ra.role_action_id as text) as roleActionIdText, CAST(ra.role_id as text) as roleIdText,
      e.entity_name as entityName, ra.permission_type as permissionType, ra.action_scope as actionScopeType,
      CAST(r.role_id as text) as roleTypeIdText, CAST(at.asset_type_id as text) as assetTypeIdText,
      CAST(att.attribute_type_id as text) as attributeTypeIdText, CAST(rt.relation_type_id as text) as relationTypeIdText,
      CAST(resp.asset_id as text) as assetIdText
    FROM role_action ra
    INNER JOIN responsibility resp on resp.role_id = ra.role_id
    INNER JOIN action_type act on act.action_type_id = ra.action_type_id
    INNER JOIN entity e on ra.entity_type_id = e.entity_id
    LEFT JOIN role r on ra.role_type_id = r.role_id
    LEFT JOIN asset_type at on ra.asset_type_id = at.asset_type_id
    LEFT JOIN attribute_type att on ra.attribute_type_id = att.attribute_type_id
    LEFT JOIN relation_type rt on ra.relation_type_id = rt.relation_type_id
    WHERE
      resp.user_id = :userId and
      resp.responsible_type = 'USER' and
      (resp.asset_id in :assetIds) and
      (act.action_type_name in :actionTypeNames) and
      (:entityNameType is null or e.entity_name = :entityNameType) and
      (:roleIdsCount = 0 or (ra.action_scope = 'ONE_ID' and r.role_id in (:roleIds)) or r.role_id is null) and
      (:assetTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and at.asset_type_id in (:assetTypeIds)) or at.asset_type_id is null) and
      (:attributeTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and att.attribute_type_id in (:attributeTypeIds)) or att.attribute_type_id is null) and
      (:relationTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and rt.relation_type_id in (:relationTypeIds)) or rt.relation_type_id is null) and
      resp.deleted_flag = false and
      ra.deleted_flag = false
  """, nativeQuery = true)
  List<RoleActionResponse> findAllByUserIdAndAssetIdResponsibilities (
    @Param(value = "userId") UUID userId,
    @Param(value = "assetIds") List<UUID> assetIds,
    @Param(value = "actionTypeNames") List<String> actionTypeNames,
    @Param(value = "entityNameType") String entityNameType,
    @Param(value = "roleIdsCount") Integer roleIdsCount,
    @Param(value = "roleIds") List<UUID> roleIds,
    @Param(value = "assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param(value = "assetTypeIds") List<UUID> assetTypeIds,
    @Param(value = "attributeTypeIdsCount") Integer attributeTypeIdsCount,
    @Param(value = "attributeTypeIds") List<UUID> attributeTypeIds,
    @Param(value = "relationTypeIdsCount") Integer relationTypeIdsCount,
    @Param(value = "relationTypeIds") List<UUID> relationTypeIds
  );

  @Cacheable(value = CACHE_NAME)
  @Query(value= """
    SELECT
      distinct CAST(ra.role_action_id as text) as roleActionIdText, CAST(ra.role_id as text) as roleIdText,
      e.entity_name as entityName, ra.permission_type as permissionType, ra.action_scope as actionScopeType,
      CAST(r.role_id as text) as roleTypeIdText, CAST(at.asset_type_id as text) as assetTypeIdText,
      CAST(att.attribute_type_id as text) as attributeTypeIdText, CAST(rt.relation_type_id as text) as relationTypeIdText,
      CAST(resp.asset_id as text) as assetIdText
    FROM role_action ra
    INNER JOIN responsibility resp on resp.role_id = ra.role_id
    INNER JOIN action_type act on act.action_type_id = ra.action_type_id
    INNER JOIN entity e on ra.entity_type_id = e.entity_id
    LEFT JOIN role r on ra.role_type_id = r.role_id
    LEFT JOIN asset_type at on ra.asset_type_id = at.asset_type_id
    LEFT JOIN attribute_type att on ra.attribute_type_id = att.attribute_type_id
    LEFT JOIN relation_type rt on ra.relation_type_id = rt.relation_type_id
    WHERE
      (resp.asset_id in :assetIds) and
      resp.responsible_type = 'GROUP' and
      resp.group_id in (
        Select group_id
        FROM user_group ug
        WHERE ug.user_id = :userId and ug.deleted_flag = false
      ) and
      (act.action_type_name in :actionTypeNames) and
      (:entityNameType is null or e.entity_name = :entityNameType) and
      (:roleIdsCount = 0 or (ra.action_scope = 'ONE_ID' and r.role_id in (:roleIds)) or r.role_id is null) and
      (:assetTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and at.asset_type_id in (:assetTypeIds)) or at.asset_type_id is null) and
      (:attributeTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and att.attribute_type_id in (:attributeTypeIds)) or att.attribute_type_id is null) and
      (:relationTypeIdsCount = 0 or (ra.action_scope = 'ONE_ID' and rt.relation_type_id in (:relationTypeIds)) or rt.relation_type_id is null) and
      resp.deleted_flag = false and
      ra.deleted_flag = false
  """, nativeQuery = true)
  List<RoleActionResponse> findAllByUserGroupsAndAssetIdResponsibilities (
    @Param(value = "userId") UUID userId,
    @Param(value = "assetIds") List<UUID> assetIds,
    @Param(value = "actionTypeNames") List<String> actionTypeNames,
    @Param(value = "entityNameType") String entityNameType,
    @Param(value = "roleIdsCount") Integer roleIdsCount,
    @Param(value = "roleIds") List<UUID> roleIds,
    @Param(value = "assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param(value = "assetTypeIds") List<UUID> assetTypeIds,
    @Param(value = "attributeTypeIdsCount") Integer attributeTypeIdsCount,
    @Param(value = "attributeTypeIds") List<UUID> attributeTypeIds,
    @Param(value = "relationTypeIdsCount") Integer relationTypeIdsCount,
    @Param(value = "relationTypeIds") List<UUID> relationTypeIds
  );

  @Query("""
    SELECT ra FROM RoleAction ra
      left join ra.actionType
      left join ra.entity
      left join ra.role
      left join ra.assetType
      left join ra.relationType
      left join ra.attributeType
    WHERE
      ra.role.roleId in :roleIds
  """)
  List<RoleAction> findAllByRoleIdsWithJoinedTables (
    @Param(value = "roleIds") List<UUID> roleIds
  );

  @Query(value= """
    SELECT ra FROM RoleAction ra
    inner join fetch ra.entity e
    inner join fetch ra.actionType at
    inner join fetch ra.role r
    left join fetch ra.attributeType
    left join fetch ra.assetType
    left join fetch ra.relationType
    left join fetch ra.roleType
    WHERE
      (cast(:roleId as org.hibernate.type.PostgresUUIDType) is null or ra.role.roleId = :roleId) and
      (cast(:entityTypeId as org.hibernate.type.PostgresUUIDType) is null or ra.entity.id = :entityTypeId) and
      (cast(:actionTypeId as org.hibernate.type.PostgresUUIDType) is null or ra.actionType.actionTypeId = :actionTypeId) and
      (:actionScope is null or ra.actionScopeType = :actionScope) and
      (:permissionType is null or ra.permissionType = :permissionType) and (
        cast(:objectTypeId as org.hibernate.type.PostgresUUIDType) is null or
          (
            ra.actionScopeType = 'ONE_ID' and (
              (ra.roleType is not null and ra.roleType.roleId = :objectTypeId) or
              (ra.assetType is not null and ra.assetType.assetTypeId = :objectTypeId) or
              (ra.relationType is not null and ra.relationType.relationTypeId = :objectTypeId) or
              (ra.attributeType is not null and ra.attributeType.attributeTypeId = :objectTypeId)
            )
          )
       ) and
     ra.isDeleted = false
  """, countQuery = """
    SELECT count(ra) FROM RoleAction ra
    WHERE
      (cast(:roleId as org.hibernate.type.PostgresUUIDType) is null or ra.role.roleId = :roleId) and
      (cast(:entityTypeId as org.hibernate.type.PostgresUUIDType) is null or ra.entity.id = :entityTypeId) and
      (cast(:actionTypeId as org.hibernate.type.PostgresUUIDType) is null or ra.actionType.actionTypeId = :actionTypeId) and
      (:actionScope is null or ra.actionScopeType = :actionScope) and
      (:permissionType is null or ra.permissionType = :permissionType) and (
        cast(:objectTypeId as org.hibernate.type.PostgresUUIDType) is null or
          (
            ra.actionScopeType = 'ONE_ID' and (
              (ra.roleType is not null and ra.roleType.roleId = :objectTypeId) or
              (ra.assetType is not null and ra.assetType.assetTypeId = :objectTypeId) or
              (ra.relationType is not null and ra.relationType.relationTypeId = :objectTypeId) or
              (ra.attributeType is not null and ra.attributeType.attributeTypeId = :objectTypeId)
            )
          )
       ) and
     ra.isDeleted = false
  """)
  Page<RoleAction> findAllByParamsWithJoinedTablesPageable (
    @Param(value = "roleId") UUID roleId,
    @Param(value = "entityTypeId") UUID entityTypeId,
    @Param(value = "actionTypeId") UUID actionTypeId,
    @Param(value = "actionScope") ActionScopeType actionScope,
    @Param(value = "permissionType") PermissionType permissionType,
    @Param(value = "objectTypeId") UUID objectTypeId,
    Pageable pageable
  );

  @Modifying
  @Query(value = """
    UPDATE role_action
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE
      (cast(:roleId as uuid) is null or role_id = :roleId) and
      (cast(:assetTypeId as uuid) is null or asset_type_id = :assetTypeId) and
      (cast(:attributeTypeId as uuid) is null or attribute_type_id = :attributeTypeId) and
      (cast(:relationTypeId as uuid) is null or relation_type_id = :relationTypeId)
  """, nativeQuery = true)
  void deleteByParams (
    @Param("roleId") UUID roleId,
    @Param("assetTypeId") UUID assetTypeId,
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("relationTypeId") UUID relationTypeId,
    @Param("userId") UUID userId
  );
}
