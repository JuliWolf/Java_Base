package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.models.CustomViewWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.CustomView;

/**
 * @author juliwolf
 */

public interface CustomViewRepository extends JpaRepository<CustomView, UUID> {

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.models.CustomViewWithConnectedValues(
      cv.customViewId,
      at.assetTypeId, at.assetTypeName,
      cv.customViewName,
      r.roleId, r.roleName,
      cv.headerRowNames, cv.headerPrepareQuery, cv.headerSelectQuery, cv.headerClearQuery,
      cv.tableColumnNames, cv.tablePrepareQuery, cv.tableSelectQuery, cv.tableClearQuery,
      cv.createdOn, cv.createdBy.userId,
      cv.lastModifiedOn, cv.lastModifiedBy.userId
    )
    From CustomView cv
    Inner join AssetType at on at.assetTypeId = cv.assetType.assetTypeId
    Left Join Role r on r.roleId = cv.role.roleId
    where
      (:customViewName is null or lower(cv.customViewName) LIKE '%' || lower(:customViewName) || '%') and
      (cast(:roleId as org.hibernate.type.PostgresUUIDType) is null OR r.roleId = :roleId) and
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null OR at.assetTypeId = :assetTypeId) and
      cv.isDeleted = false
  """, countQuery = """
    Select count(cv.customViewId)
    From CustomView cv
    Inner join AssetType at on at.assetTypeId = cv.assetType.assetTypeId
    Left Join Role r on r.roleId = cv.role.roleId
    where
      (:customViewName is null or lower(cv.customViewName) LIKE '%' || lower(:customViewName) || '%') and
      (cast(:roleId as org.hibernate.type.PostgresUUIDType) is null OR r.roleId = :roleId) and
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null OR at.assetTypeId = :assetTypeId) and
      cv.isDeleted = false
  """)
  Page<CustomViewWithConnectedValues> findAllByParamsPageable (
    @Param("roleId") UUID roleId,
    @Param("assetTypeId") UUID assetTypeId,
    @Param("customViewName") String customViewName,
    PageRequest pageRequest
  );

  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.models.CustomViewWithConnectedValues(
      cv.customViewId,
      at.assetTypeId, at.assetTypeName,
      cv.customViewName,
      r.roleId, r.roleName,
      cv.headerRowNames, cv.headerPrepareQuery, cv.headerSelectQuery, cv.headerClearQuery,
      cv.tableColumnNames, cv.tablePrepareQuery, cv.tableSelectQuery, cv.tableClearQuery,
      cv.createdOn, cv.createdBy.userId,
      cv.lastModifiedOn, cv.lastModifiedBy.userId
    )
    From CustomView cv
    Inner join AssetType at on at.assetTypeId = cv.assetType.assetTypeId
    Left Join Role r on r.roleId = cv.role.roleId
    where
      cv.customViewId = :customViewId and
      cv.isDeleted = false
  """)
  Optional<CustomViewWithConnectedValues> findByIdWithJoinedTables (
    @Param("customViewId") UUID customViewId
  );

  @Modifying
  @Query(value = """
    UPDATE custom_view
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      (cast(:assetTypeId as uuid) is null or asset_type_id = :assetTypeId) and
      (cast(:roleId as uuid) is null or role_id = :roleId)
  """, nativeQuery = true)
  void deleteByParams (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("roleId") UUID roleId,
    @Param("userId") UUID userId
  );
}
