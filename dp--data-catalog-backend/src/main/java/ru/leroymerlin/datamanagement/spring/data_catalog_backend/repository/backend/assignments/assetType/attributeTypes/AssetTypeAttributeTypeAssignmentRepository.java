package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeAttributeTypeAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeAttributeTypeAssignmentWithAllowedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeIdAttributeTypeIdAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeAttributeTypeAssignment;

/**
 * @author JuliWolf
 */
public interface AssetTypeAttributeTypeAssignmentRepository extends JpaRepository<AssetTypeAttributeTypeAssignment, UUID> {
  @Query(value = """
    SELECT atata FROM AssetTypeAttributeTypeAssignment atata
      left join fetch atata.attributeType atbt
      left join fetch atata.assetType ast
      left join fetch atata.createdBy cb
    WHERE atata.assetTypeAttributeTypeAssignmentId = :assetTypeAttributeTypeAssignmentId
  """)
  Optional<AssetTypeAttributeTypeAssignment> findByIdWithJoinedTables (
    @Param("assetTypeAttributeTypeAssignmentId") UUID assetTypeAttributeTypeAssignmentId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeAttributeTypeAssignmentWithAllowedValues(
      ast.assetTypeId, ast.assetTypeName,
      atata.assetTypeAttributeTypeAssignmentId,
      atbt.attributeTypeId, atbt.attributeTypeName, atbt.attributeTypeDescription,
      atbt.attributeKindType, atbt.validationMask, STRING_AGG(alv.value, ';'),
      atata.isInherited,
      past.assetTypeId, past.assetTypeName,
      atata.createdOn, atata.createdBy.userId
    )
    FROM AssetTypeAttributeTypeAssignment atata
      left join AttributeType atbt on atbt.attributeTypeId = atata.attributeType.attributeTypeId
      left join AttributeTypeAllowedValue alv on alv.attributeType.attributeTypeId = atbt.attributeTypeId
      left join AssetType ast on ast.assetTypeId = atata.assetType.assetTypeId
      left join AssetType past on past.assetTypeId = atata.parentAssetType.assetTypeId
    WHERE
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null or atata.assetType.assetTypeId = :assetTypeId) and
      atata.isDeleted = false
      GROUP BY atata.assetTypeAttributeTypeAssignmentId, ast.assetTypeId, atbt.attributeTypeId, past.assetTypeId
    order by atbt.attributeTypeName
  """)
  List<AssetTypeAttributeTypeAssignmentWithAllowedValues> findAllByAssetTypeIdWithJoinedTables (
    @Param("assetTypeId") UUID assetTypeId
  );

  @Query(value = """
    SELECT atata FROM AssetTypeAttributeTypeAssignment atata
      left join fetch atata.attributeType atbt
      left join fetch atata.assetType ast
      left join fetch atata.createdBy cb
    WHERE
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null or atata.assetType.assetTypeId = :assetTypeId) and
      atata.isDeleted = false
  """)
  List<AssetTypeAttributeTypeAssignment> findAllByAssetTypeId (
    @Param("assetTypeId") UUID assetTypeId
  );

  @Query(value = """
    SELECT count(asset_type_attribute_type_assignment_id) > 0 FROM "asset_type_attribute_type_assignment" atata WHERE
      atata.asset_type_id = :assetTypeId and
      atata.attribute_type_id = :attributeTypeId and
      atata.deleted_flag = false
  """, nativeQuery = true)
  Boolean isAssetTypeAttributeTypeAssignmentExistsByAssetTypeIdAndAttributeTypeId (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value = """
    with count_select as(
        select
            attribute_type_id,
            asset_type_id,
            count(*) as cnt
        from attribute att
        inner join asset a on att.asset_id = a.asset_id and a.deleted_flag = false
        where att.deleted_flag = false
        group by attribute_type_id, asset_type_id
    )
    SELECT
          cast(atata.asset_type_attribute_type_assignment_id as text) as assetTypeAttributeTypeAssignmentIdText,
          cast(ast.asset_type_id as text) as assetTypeIdText,
          ast.asset_type_name as assetTypeName,
          cast(atbt.attribute_type_id as text) as attributeTypeIdText,
          atbt.attribute_type_name as attributeTypeName,
          coalesce(cs.cnt, 0) as count,
          atata.inherited_flag as isInherited,
          cast(past.asset_type_id as text) as parentAssetTypeIdText,
          past.asset_type_name as parentAssetTypeName,
          atata.created_on as createdOn,
          cast(atata.created_by as text) as createdByText
    FROM asset_type_attribute_type_assignment atata
      left join attribute_type atbt on atbt.attribute_type_id = atata.attribute_type_id and atbt.deleted_flag = false
      left join asset_type ast on ast.asset_type_id = atata.asset_type_id and ast.deleted_flag = false
      left join asset_type past on past.asset_type_id = atata.parent_asset_type_id and past.deleted_flag = false
      left join count_select cs on cs.asset_type_id = atata.asset_type_id and cs.attribute_type_id = atata.attribute_type_id
    WHERE
        (cast(:assetTypeId as uuid) is null or atata.asset_type_id = :assetTypeId) and
        (cast(:attributeTypeId as uuid) is null or atata.attribute_type_id = :attributeTypeId) and
        atata.deleted_flag = false
  """, countQuery = """
    SELECT count(*)
    FROM asset_type_attribute_type_assignment atata
    WHERE
      (cast(:assetTypeId as uuid) is null or atata.asset_type_id = :assetTypeId) and
      (cast(:attributeTypeId as uuid) is null or atata.attribute_type_id = :attributeTypeId) and
      atata.deleted_flag = false
  """, nativeQuery = true)
  Page<AssetTypeAttributeTypeAssignmentResponse> findAllByAssetTypeIdWithJoinedTablesPageable (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("attributeTypeId") UUID attributeTypeId,
    Pageable pageable
  );

  @Query(value= """
    with afd as
    (
      select asset_type_id, attribute_type_id
      from asset_type_attribute_type_assignment atata
      where atata.asset_type_attribute_type_assignment_id = :assetTypeAttributeTypeAssignmentId
    )

    select * from asset_type_attribute_type_assignment atata
      where
        atata.attribute_type_id = (select attribute_type_id from afd) and
        atata.parent_asset_type_id = (select asset_type_id from afd) and
        deleted_flag = false
  """, nativeQuery = true)
  List<AssetTypeAttributeTypeAssignment> findAllChildAssignmentsByAssetTypeAttributeTypeId (
    @Param("assetTypeAttributeTypeAssignmentId") UUID assetTypeAttributeTypeAssignmentId
  );


  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeIdAttributeTypeIdAssignment(
      atata.attributeType.attributeTypeId, atata.assetType.assetTypeId
    )
    FROM AssetTypeAttributeTypeAssignment atata
    where
      atata.attributeType.attributeTypeId in :attributeTypeIds and
      atata.assetType.assetTypeId in :assetTypeIds and
      atata.isDeleted = false
  """)
  List<AssetTypeIdAttributeTypeIdAssignment> findAllAssetTypeAttributeTypeAssignmentsByAttributeTypeIdsAndAssetIds (
    @Param("attributeTypeIds") List<UUID> attributeTypeIds,
    @Param("assetTypeIds") List<UUID> assetTypeIds
  );

  @Modifying
  @Query(value = """
    UPDATE asset_type_attribute_type_assignment
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      (cast(:assetTypeId as uuid) is null or asset_type_id = :assetTypeId) and
      (cast(:attributeTypeId as uuid) is null or attribute_type_id = :attributeTypeId)
  """, nativeQuery = true)
  void deleteByParams (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("userId") UUID userId
  );
}
