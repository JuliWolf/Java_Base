package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationComponentAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.models.RelationComponentAttributeWithConnectedValues;

public interface RelationComponentAttributeRepository extends JpaRepository<RelationComponentAttribute, UUID> {

  @Query("""
    Select rcat
    from RelationComponentAttribute rcat
    left join fetch AttributeType at on at.attributeTypeId = rcat.attributeType.attributeTypeId
    left join fetch Language l on l.languageId = rcat.language.languageId
    where
      rcat.relationComponentAttributeId = :relationComponentAttributeId
  """)
  Optional<RelationComponentAttribute> findRelationComponentAttributeByIdWithJoinedTables (
    @Param("relationComponentAttributeId") UUID relationComponentAttributeId
  );

  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.models.RelationComponentAttributeWithConnectedValues(
      rcat.relationComponentAttributeId,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType,
      rcat.relationComponent.relationComponentId, rcat.value,
      rcat.isInteger, rcat.valueNumeric, rcat.valueBoolean, rcat.valueDatetime,
      l.language, rcat.createdOn, rcat.createdBy.userId,
      rcat.lastModifiedOn, rcat.modifiedBy.userId
    )
    from RelationComponentAttribute rcat
    left join AttributeType at on at.attributeTypeId = rcat.attributeType.attributeTypeId
    left join Language l on l.languageId = rcat.language.languageId
    Where
      rcat.relationComponentAttributeId = :relationComponentAttributeId and
      rcat.isDeleted = false
  """)
  Optional<RelationComponentAttributeWithConnectedValues> findRelationComponentAttributeById (
    @Param("relationComponentAttributeId") UUID relationComponentAttributeId
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.models.RelationComponentAttributeWithConnectedValues(
      rcat.relationComponentAttributeId,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType,
      rcat.relationComponent.relationComponentId, rcat.value,
      rcat.isInteger, rcat.valueNumeric, rcat.valueBoolean, rcat.valueDatetime,
      l.language, rcat.createdOn, rcat.createdBy.userId,
      rcat.lastModifiedOn, rcat.modifiedBy.userId
    )
    from RelationComponentAttribute rcat
    left join AttributeType at on at.attributeTypeId = rcat.attributeType.attributeTypeId
    left join Language l on l.languageId = rcat.language.languageId
    where
      (:attributeTypeIdsCount = 0 OR rcat.attributeType.attributeTypeId in :attributeTypeIds) and
      (:relationComponentIdsCount = 0 OR rcat.relationComponent.relationComponentId in :relationComponentIds) and
      rcat.isDeleted = false
  """, countQuery = """
      Select count(rcat.relationComponentAttributeId)
      from RelationComponentAttribute rcat
      left join AttributeType at on at.attributeTypeId = rcat.attributeType.attributeTypeId
      left join Language l on l.languageId = rcat.language.languageId
      where
        (:attributeTypeIdsCount = 0 OR rcat.attributeType.attributeTypeId in :attributeTypeIds) and
        (:relationComponentIdsCount = 0 OR rcat.relationComponent.relationComponentId in :relationComponentIds) and
        rcat.isDeleted = false
  """)
  Page<RelationComponentAttributeWithConnectedValues> findRelationComponentAttributesByParamsPageable (
    @Param("attributeTypeIdsCount") int attributeTypeIdsCount,
    @Param("attributeTypeIds") List<UUID> attributeTypeIds,
    @Param("relationComponentIdsCount") int relationComponentIdsCount,
    @Param("relationComponentIds") List<UUID> relationComponentIds,
    Pageable pageable
  );

  @Query(value = """
    SELECT count(rca.relationComponentAttributeId) > 0
    FROM RelationComponentAttribute rca
    WHERE
      rca.attributeType.attributeTypeId = :attributeTypeId and
      rca.isDeleted = false
  """)
  Boolean isRelationComponentAttributesExistsByAttributeType (
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value = """
    SELECT rca
    FROM RelationComponentAttribute rca
    left join RelationComponent rc on rc.relationComponentId = rca.relationComponent.relationComponentId
    WHERE
      rc.relation.relationId in :relationIds and
      rca.isDeleted = false
  """)
  List<RelationComponentAttribute> findAllByRelationIds (
    @Param("relationIds") List<UUID> relationIds
  );

  @Modifying
  @Query(value = """
    UPDATE relation_component_attribute rca
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE rca.relation_component_id in (
      Select rc.relation_component_id
      From relation_component_attribute rca
      left join relation_component rc on rc.relation_component_id = rca.relation_component_id
      Where rc.relation_id in :relationIds
    )
  """, nativeQuery = true)
  void deleteAllByRelationIds (
    @Param("relationIds") List<UUID> relationIds,
    @Param("userId") UUID userId
  );

  @Query(value= """
    SELECT rca.relation_component_attribute_id
    FROM relation_component_attribute rca
    INNER JOIN attribute_type at on rca.attribute_type_id = at.attribute_type_id
    Where
       rca.attribute_type_id = :attributeTypeId and
       (at.attribute_kind = 'SINGLE_VALUE_LIST' or at.attribute_kind = 'MULTIPLE_VALUE_LIST') and
       :value = any(STRING_TO_ARRAY(rca.value, ';')) and
       at.deleted_flag = false and
       rca.deleted_flag = false
    ORDER BY rca.relation_component_attribute_id DESC
    LIMIT 1
  """, nativeQuery = true)
  UUID findFirstRelationComponentAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue (
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("value") String value
  );

  @Query(value = """
    SELECT count(rca.relationComponentAttributeId) > 0
    FROM RelationComponentAttribute rca
    Inner join RelationComponent rc on rc.relationComponentId = rca.relationComponent.relationComponentId
    WHERE
      rc.relationTypeComponent.relationTypeComponentId = :relationTypeComponentId and
      rca.isDeleted = false
  """)
  Boolean isRelationComponentAttributesExistsByRelationTypeComponentId (
    @Param("relationTypeComponentId") UUID relationTypeComponentId
  );
}
