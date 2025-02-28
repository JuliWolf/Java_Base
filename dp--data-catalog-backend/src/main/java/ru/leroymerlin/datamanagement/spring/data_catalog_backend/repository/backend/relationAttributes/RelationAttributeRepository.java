package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.models.RelationAttributeWithConnectedValues;

/**
 * @author juliwolf
 */

public interface RelationAttributeRepository extends JpaRepository<RelationAttribute, UUID> {
  @Query("""
    Select rat
    from RelationAttribute rat
    left join fetch AttributeType at on at.attributeTypeId = rat.attributeType.attributeTypeId
    left join fetch Language l on l.languageId = rat.language.languageId
    where
      rat.relationAttributeId = :relationAttributeId
  """)
  Optional<RelationAttribute> findRelationAttributeByIdWithJoinedTables (
    @Param("relationAttributeId") UUID relationAttributeId
  );

  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.models.RelationAttributeWithConnectedValues(
      rat.relationAttributeId,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType,
      rat.relation.relationId, rat.value,
      rat.isInteger, rat.valueNumeric, rat.valueBoolean, rat.valueDatetime,
      l.language, rat.createdOn, rat.createdBy.userId,
      rat.lastModifiedOn, rat.modifiedBy.userId
    )
    from RelationAttribute rat
    left join AttributeType at on at.attributeTypeId = rat.attributeType.attributeTypeId
    left join Language l on l.languageId = rat.language.languageId
    where
      rat.relationAttributeId = :relationAttributeId and
      rat.isDeleted = false
  """)
  Optional<RelationAttributeWithConnectedValues> findRelationAttributeById (
    @Param("relationAttributeId") UUID relationAttributeId
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.models.RelationAttributeWithConnectedValues(
      rat.relationAttributeId,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType,
      rat.relation.relationId, rat.value,
      rat.isInteger, rat.valueNumeric, rat.valueBoolean, rat.valueDatetime,
      l.language, rat.createdOn, rat.createdBy.userId,
      rat.lastModifiedOn, rat.modifiedBy.userId
    )
    from RelationAttribute rat
    left join AttributeType at on at.attributeTypeId = rat.attributeType.attributeTypeId
    left join Language l on l.languageId = rat.language.languageId
    where
      (cast(:relationId as org.hibernate.type.PostgresUUIDType) is null OR rat.relation.relationId = :relationId) and
      (:attributeTypeIdsCount = 0 OR rat.attributeType.attributeTypeId in :attributeTypeIds) and
      rat.isDeleted = false
    Order by at.attributeTypeName
  """, countQuery = """
    Select count(rat.relationAttributeId)
    from RelationAttribute rat
    left join AttributeType at on at.attributeTypeId = rat.attributeType.attributeTypeId
    left join Language l on l.languageId = rat.language.languageId
    where
      (cast(:relationId as org.hibernate.type.PostgresUUIDType) is null OR rat.relation.relationId = :relationId) and
      (:attributeTypeIdsCount = 0 OR rat.attributeType.attributeTypeId in :attributeTypeIds) and
      rat.isDeleted = false
  """)
  Page<RelationAttributeWithConnectedValues> findRelationAttributesByParamsPageable (
    @Param("relationId") UUID relationId,
    @Param("attributeTypeIdsCount") Integer attributeTypeIdsCount,
    @Param("attributeTypeIds") List<UUID> attributeTypeIds,
    Pageable pageable
  );

  @Query(value = """
    SELECT count(ra.relationAttributeId) > 0
    FROM RelationAttribute ra
    WHERE
      ra.attributeType.attributeTypeId = :attributeTypeId and
      ra.isDeleted = false
  """)
  Boolean isRelationAttributesExistsByAttributeType (
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Modifying
  @Query(value = """
    UPDATE relation_attribute
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
    SELECT a.relation_attribute_id
    FROM relation_attribute a
    INNER JOIN attribute_type at on a.attribute_type_id = at.attribute_type_id
    Where
       a.attribute_type_id = :attributeTypeId and
       (at.attribute_kind = 'SINGLE_VALUE_LIST' or at.attribute_kind = 'MULTIPLE_VALUE_LIST') and
       :value = any(STRING_TO_ARRAY(a.value, ';')) and
       at.deleted_flag = false and
       a.deleted_flag = false
    ORDER BY a.relation_attribute_id DESC
    LIMIT 1
  """, nativeQuery = true)
  UUID findFirstRelationAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue (
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("value") String value
  );

  @Query(value = """
    SELECT count(ra.relationAttributeId) > 0
    FROM RelationAttribute ra
    Inner join Relation r on r.relationId = ra.relation.relationId
    WHERE
      r.relationType.relationTypeId = :relationTypeId and
      ra.isDeleted = false
  """)
  Boolean isRelationAttributesExistsByRelationTypeId (
    @Param("relationTypeId") UUID relationTypeId
  );
}
