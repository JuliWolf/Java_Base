package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.models.RelationTypeComponentAttributeTypeAssignmentUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.models.RelationTypeAttributeTypeAssignmentWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeAttributeTypeAssignment;

/**
 * @author juliwolf
 */

public interface RelationTypeAttributeTypeAssignmentRepository extends JpaRepository<RelationTypeAttributeTypeAssignment, UUID> {

  @Query(value = """
    SELECT rtata FROM RelationTypeAttributeTypeAssignment rtata
      left join fetch rtata.relationType rt
      left join fetch rtata.attributeType at
      left join fetch rtata.createdBy cb
    WHERE rtata.relationTypeAttributeTypeAssignmentId = :relationTypeAttributeTypeAssignmentId
  """)
  Optional<RelationTypeAttributeTypeAssignment> findByIdWithJoinedTables (
    @Param("relationTypeAttributeTypeAssignmentId") UUID relationTypeAttributeTypeAssignmentId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.models.RelationTypeAttributeTypeAssignmentWithConnectedValues(
      rt.relationTypeId, rt.relationTypeName,
      rtata.relationTypeAttributeTypeAssignmentId,
      at.attributeTypeId, at.attributeTypeName,
      rtata.createdOn, cb.userId
    )
    FROM RelationTypeAttributeTypeAssignment rtata
      left join rtata.relationType rt
      left join rtata.attributeType at
      left join rtata.createdBy cb
    WHERE
      rtata.relationType.relationTypeId = :relationTypeId and
      rtata.isDeleted = false
    order by at.attributeTypeName
  """)
  List<RelationTypeAttributeTypeAssignmentWithConnectedValues> findAllByRelationTypeIdWithJoinedTables (
    @Param("relationTypeId") UUID relationTypeId
  );

  @Query(value = """
    SELECT count(rtata.relationTypeAttributeTypeAssignmentId) > 0
    FROM RelationTypeAttributeTypeAssignment rtata
    WHERE
      rtata.relationType.relationTypeId = :relationTypeId and
      rtata.attributeType.attributeTypeId = :attributeTypeId and
      rtata.isDeleted = false
  """)
  Boolean isAssignmentsExistsByRelationTypeAndAttributeType (
    @Param("relationTypeId") UUID relationTypeId,
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(
    value = """
      with count_select as (
          select attribute_type_id, relation_type_id, count(*) as cnt
          from relation_attribute ratt
          inner join relation r on ratt.relation_id = r.relation_id
          where ratt.deleted_flag = false
          group by attribute_type_id, relation_type_id
      )
      SELECT
        cast(rtata.relation_type_attribute_type_assignment_id as text) as relationTypeAttributeTypeAssignmentIdText,
        cast(rtata.relation_type_id as text) as relationTypeIdText,
        rt.relation_type_name as relationTypeName,
        cast(at.attribute_type_id as text) as attributeTypeIdText,
        at.attribute_type_name as attributeTypeName,
        coalesce(cs.cnt, 0) as count,
        rtata.created_on as createdOn,
        cast(rtata.created_by as text) as createdByText
      FROM relation_type_attribute_type_assignment rtata
      inner join relation_type rt on rtata.relation_type_id = rt.relation_type_id and rt.deleted_flag = false
      inner join attribute_type at on rtata.attribute_type_id = at.attribute_type_id and at.deleted_flag = false
      left join count_select cs on cs.attribute_type_id = rtata.attribute_type_id and cs.relation_type_id = rtata.relation_type_id
      WHERE
        (cast(:relationTypeId as uuid) is null or rtata.relation_type_id = :relationTypeId) and
        (cast(:attributeTypeId as uuid) is null or rtata.attribute_type_id = :attributeTypeId) and
        rtata.deleted_flag = false
    """,
    countQuery = """
      SELECT count(*)
      FROM relation_type_attribute_type_assignment rtata
      inner join relation_type rt on rtata.relation_type_id = rt.relation_type_id and rt.deleted_flag = false
      inner join attribute_type at on rtata.attribute_type_id = at.attribute_type_id and at.deleted_flag = false
      WHERE
        (cast(:relationTypeId as uuid) is null or rtata.relation_type_id = :relationTypeId) and
        (cast(:attributeTypeId as uuid) is null or rtata.attribute_type_id = :attributeTypeId) and
        rtata.deleted_flag = false
    """, nativeQuery = true)
  Page<RelationTypeComponentAttributeTypeAssignmentUsageCount> findAllWithUsageCountPageable (
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("relationTypeId") UUID relationTypeId,
    Pageable pageable
  );

  @Modifying
  @Query(value = """
    UPDATE relation_type_attribute_type_assignment
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE
      (cast(:attributeTypeId as uuid) is null or attribute_type_id = :attributeTypeId) and
      (cast(:relationTypeId as uuid) is null or relation_type_id = :relationTypeId)
  """, nativeQuery = true)
  void deleteByParams (
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("relationTypeId") UUID relationTypeId,
    @Param("userId") UUID userId
  );
}
