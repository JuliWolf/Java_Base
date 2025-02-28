package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetIdResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models.AttributeWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models.ReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Attribute;

/**
 * @author JuliWolf
 */
public interface AttributeRepository extends JpaRepository<Attribute, UUID> {
  @Query(value = """
    SELECT a FROM Attribute a
    WHERE
      a.attributeType.attributeTypeId = :attributeTypeId and
      a.isDeleted = false
  """)
  List<Attribute> findAllByAttributeTypeId (
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models.AttributeWithConnectedValues(
      atb.attributeId,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType,
      a.assetId, a.assetDisplayName, a.assetName,
      atb.value, atb.isInteger, atb.valueNumeric, atb.valueBoolean, atb.valueDatetime,
      l.language,
      atb.createdOn, cb.userId,
      atb.lastModifiedOn, mb.userId
    )
    FROM Attribute atb
      left join atb.asset a
      left join atb.createdBy cb
      left join atb.attributeType at
      left join atb.language l
      left join atb.modifiedBy mb
    WHERE
      atb.attributeId = :attributeId and
      atb.isDeleted = false
  """)
  Optional<AttributeWithConnectedValues> findAttributeByIdWithJoinedTables (
    @Param("attributeId") UUID attributeId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models.AttributeWithConnectedValues(
      atb.attributeId,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType,
      a.assetId, a.assetDisplayName, a.assetName,
      atb.value, atb.isInteger, atb.valueNumeric, atb.valueBoolean, atb.valueDatetime,
      l.language,
      atb.createdOn, cb.userId,
      atb.lastModifiedOn, mb.userId
    )
    FROM Attribute atb
      left join atb.asset a
      left join atb.createdBy cb
      left join atb.attributeType at
      left join atb.language l
      left join atb.modifiedBy mb
    WHERE
      (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null OR atb.asset.assetId = :assetId) and
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null OR atb.attributeType.attributeTypeId = :assetTypeId) and
      atb.isDeleted = false
  """, countQuery = """
    SELECT count(atb.attributeId) FROM Attribute atb
    WHERE
      (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null OR atb.asset.assetId = :assetId) and
      (cast(:assetTypeId as org.hibernate.type.PostgresUUIDType) is null OR atb.attributeType.attributeTypeId = :assetTypeId) and
      atb.isDeleted = false
  """)
  Page<AttributeWithConnectedValues> findAllByParamsWithJoinedTablesPageable (
    @Param("assetId") UUID assetId,
    @Param("assetTypeId") UUID assetTypeId,
    Pageable pageable
  );

  @Query(value = """
    SELECT count(a.attributeId) > 0 FROM Attribute a
    WHERE
      a.attributeType.attributeTypeId = :attributeTypeId and
      a.isDeleted = false
  """)
  Boolean isAttributeExistsByAttributeTypeId (
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value = """
    SELECT CAST(a.asset_id as text) as assetIdText
    FROM attribute a
    Inner join asset ast on a.asset_id = ast.asset_id
    WHERE
      ast.asset_type_id = :assetTypeId and
      a.attribute_type_id = :attributeTypeId and
      a.deleted_flag = false
    LIMIT 1
  """, nativeQuery = true)
  List<AssetIdResponse> findFirstAssetIdByAssetTypeIdAndAttributeTypeId (
    @Param("assetTypeId") UUID assetTypeId,
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value= """
    SELECT a.attribute_id
    FROM attribute a
    INNER JOIN attribute_type at on a.attribute_type_id = at.attribute_type_id
    Where
       a.attribute_type_id = :attributeTypeId and
       (at.attribute_kind = 'SINGLE_VALUE_LIST' or at.attribute_kind = 'MULTIPLE_VALUE_LIST') and
       :value = any(STRING_TO_ARRAY(a.value, ';')) and
       at.deleted_flag = false and
       a.deleted_flag = false
    ORDER BY a.attribute_id DESC
    LIMIT 1
  """, nativeQuery = true)
  UUID findFirstAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue (
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("value") String value
  );

  @Query(value = """
    SELECT a
    FROM Attribute a
    WHERE
      a.attributeId in :attributeIds and
      a.isDeleted = false
  """)
  List<Attribute> findAllByAttributeIds (
    @Param("attributeIds") List<UUID> attributeIds
  );

  @Modifying
  @Query(value = """
    UPDATE attribute
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      attribute.attribute_id in :attributeIds
  """, nativeQuery = true)
  void deleteAllByAttributeIds (
    @Param("attributeIds") List<UUID> attributeIds,
    @Param("userId") UUID userId
  );

  @Modifying
  @Query(value = """
    UPDATE attribute
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      attribute.asset_id in :assetIds
  """, nativeQuery = true)
  void deleteAllByAssetIds (
    @Param("assetIds") List<UUID> assetIds,
    @Param("userId") UUID userId
  );

  @Query(value = """
    SELECT a.attributeId
    FROM Attribute a
    WHERE
      a.attributeId in :attributeIds and
      a.isDeleted = false
  """)
  List<UUID> findIdsByAttributeIds (
    @Param("attributeIds") List<UUID> attributeIds
  );

  @Query(value = """
    select
      attr.value as reportReviewRequestId, attr.created_on as reportReviewRequestCreationDate,
      closedAttr.value_datetime as reportReviewRequestClosedDate,
      attr.asset_id as reportDataCatalogId, u.username as ldap
    from attribute attr
    left join attribute closedAttr on not closedAttr.deleted_flag and closedAttr.asset_id = attr.asset_id and closedAttr.attribute_type_id = '685be2d1-9f50-4ec7-a137-aa2bbfe8c0fb'
    left join responsibility resp on resp.asset_id = attr.asset_id and not resp.deleted_flag and resp.role_id = 'dd2fce3b-ad2c-42f1-ba53-941ceae7be0d'
    left join "user" u on u.user_id = resp.user_id
    where
      (:reviewerLdap is null or u.username = :reviewerLdap) and
      (:reportDataCatalogId is null or cast(attr.asset_id as text) = :reportDataCatalogId) and
      attr.deleted_flag = false and
      attr.attribute_type_id = '20e752bb-5e3c-45b6-9141-979be8aa874d'
  """,
  countQuery = """
    select count(attr.asset_id)
      from attribute attr
      left join attribute closedAttr on not closedAttr.deleted_flag and closedAttr.asset_id = attr.asset_id and closedAttr.attribute_type_id = '685be2d1-9f50-4ec7-a137-aa2bbfe8c0fb'
      left join responsibility resp on resp.asset_id = attr.asset_id and not resp.deleted_flag and resp.role_id = 'dd2fce3b-ad2c-42f1-ba53-941ceae7be0d'
      left join "user" u on u.user_id = resp.user_id
      where
        (:reviewerLdap is null or u.username = :reviewerLdap) and
        (:reportDataCatalogId is null or cast(attr.asset_id as text) = :reportDataCatalogId) and
        attr.deleted_flag = false and
        attr.attribute_type_id = '20e752bb-5e3c-45b6-9141-979be8aa874d'
  """, nativeQuery = true)
  Page<ReportReviewRequest> getReportReviewRequestsByParamsPageable (
    @Param("reviewerLdap") String reviewerLdap,
    @Param("reportDataCatalogId") String reportDataCatalogId,
    Pageable pageable
  );

  @Query(value = """
    select
      attr.value as reportReviewRequestId, attr.created_on as reportReviewRequestCreationDate,
      closedAttr.value_datetime as reportReviewRequestClosedDate,
      attr.asset_id as reportDataCatalogId, u.username as ldap
    from attribute attr
    left join attribute closedAttr on not closedAttr.deleted_flag and closedAttr.asset_id = attr.asset_id and closedAttr.attribute_type_id = '685be2d1-9f50-4ec7-a137-aa2bbfe8c0fb'
    left join responsibility resp on resp.asset_id = attr.asset_id and not resp.deleted_flag and resp.role_id = 'dd2fce3b-ad2c-42f1-ba53-941ceae7be0d'
    left join "user" u on u.user_id = resp.user_id
    where
      attr.value = :reportId and
      attr.deleted_flag = false and
      attr.attribute_type_id = '20e752bb-5e3c-45b6-9141-979be8aa874d'
    limit 1
  """, nativeQuery = true)
  Optional<ReportReviewRequest> getReportReviewRequestByReportId (
    @Param("reportId") String reportId
  );

  @Query(value = """
    select count(attr.attribute_id) > 0
    from attribute attr
    where
      attr.value = :reportId and
      attr.deleted_flag = false and
      attr.attribute_type_id = '20e752bb-5e3c-45b6-9141-979be8aa874d'
    limit 1
  """, nativeQuery = true)
  Boolean isReportReviewExistsByReportId (
    @Param("reportId") String reportId
  );

  @Query(value = """
    Select attr.*
    from attribute attr
    WHERE
      attr.value = :reportReviewId and
      attr.deleted_flag = false and
      attr.attribute_type_id = '20e752bb-5e3c-45b6-9141-979be8aa874d'
      limit 1
  """, nativeQuery = true)
  Optional<Attribute> findReportRequestByReportReviewId (
    @Param("reportReviewId") String reportReviewId
  );


  @Query("""
    Select attr
    From Attribute attr
    Where
      attr.asset.assetId = :assetId and
      attr.attributeType.attributeTypeId = :attributeTypeId and
      attr.isDeleted = false
  """)
  Optional<Attribute> findFirstAttributeByParams (
    @Param("assetId") UUID assetId,
    @Param("attributeTypeId") UUID attributeTypeId
  );
}
