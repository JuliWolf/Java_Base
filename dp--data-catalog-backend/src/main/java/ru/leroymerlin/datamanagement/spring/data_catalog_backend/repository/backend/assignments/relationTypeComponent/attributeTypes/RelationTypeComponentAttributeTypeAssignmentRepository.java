package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAttributeTypeAssignmentUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.models.RelationTypeComponentAttributeTypeAssignmentWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponentAttributeTypeAssignment;

/**
 * @author juliwolf
 */

public interface RelationTypeComponentAttributeTypeAssignmentRepository extends JpaRepository<RelationTypeComponentAttributeTypeAssignment, UUID> {
  @Query("""
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.models.RelationTypeComponentAttributeTypeAssignmentWithConnectedValues(
      rtc.relationTypeComponentId, rtc.relationTypeComponentName,
      rtcata.relationTypeComponentAttributeTypeAssignmentId,
      at.attributeTypeId, at.attributeTypeName, at.attributeKindType, at.validationMask,
      STRING_AGG(alv.value, ';'),
      rtcata.createdOn, rtcata.createdBy.userId
    )
    FROM RelationTypeComponentAttributeTypeAssignment rtcata
    inner join RelationTypeComponent rtc on rtc.relationTypeComponentId = rtcata.relationTypeComponent.relationTypeComponentId
    inner join AttributeType at on at.attributeTypeId = rtcata.attributeType.attributeTypeId
    left join AttributeTypeAllowedValue alv on alv.attributeType.attributeTypeId = at.attributeTypeId
    WHERE
      rtc.relationTypeComponentId = :relationTypeComponentId and
      rtcata.isDeleted = false
    GROUP BY at.attributeTypeId, rtc.relationTypeComponentId, rtcata.createdBy.userId, rtcata.relationTypeComponentAttributeTypeAssignmentId
    order by at.attributeTypeName
  """)
  List<RelationTypeComponentAttributeTypeAssignmentWithConnectedValues> findAllWithJoinedTablesByRelationTypeComponentId (
    @Param("relationTypeComponentId") UUID relationTypeComponentId
  );

  @Query(value = """
    SELECT count(rtcata.relationTypeComponentAttributeTypeAssignmentId) > 0
    FROM RelationTypeComponentAttributeTypeAssignment rtcata
    WHERE
      rtcata.relationTypeComponent.relationTypeComponentId = :relationTypeComponentId and
      rtcata.attributeType.attributeTypeId = :attributeTypeId and
      rtcata.isDeleted = false
  """)
  Boolean isAssignmentsExistsByRelationTypeComponentAndAttributeType (
    @Param("relationTypeComponentId") UUID relationTypeComponentId,
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value = """
    SELECT rtcata
    FROM RelationTypeComponentAttributeTypeAssignment rtcata
    WHERE
      rtcata.attributeType.attributeTypeId = :attributeTypeId and
      rtcata.isDeleted = false
  """)
  List<RelationTypeComponentAttributeTypeAssignment> findAllByAttributeTypeId (
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value = """
    SELECT rtcata
    FROM RelationTypeComponentAttributeTypeAssignment rtcata
    WHERE
      rtcata.relationTypeComponent.relationTypeComponentId in :relationTypeComponentIds and
      rtcata.isDeleted = false
  """)
  List<RelationTypeComponentAttributeTypeAssignment> findAllByRelationTypeComponentIds (
    @Param("relationTypeComponentIds") List<UUID> relationTypeComponentIds
  );

  @Query(
    value = """
      with count_select as (
          select attribute_type_id, relation_type_component_id, count(*) as cnt
          from relation_component_attribute rcatt
          inner join relation_component rc on rcatt.relation_component_id = rc.relation_component_id
          where rcatt.deleted_flag = false
          group by attribute_type_id, relation_type_component_id
      )
      SELECT
        cast(rtcata.relation_type_component_attribute_type_assignment_id as text) as relationTypeComponentAttributeTypeAssignmentIdText,
        cast(rtcata.relation_type_component_id as text) as relationTypeComponentIdText,
        rtc.relation_type_component_name as relationTypeComponentName,
        cast(at.attribute_type_id as text) as attributeTypeIdText,
        at.attribute_type_name as attributeTypeName,
        coalesce(cs.cnt, 0) as count,
        rtcata.created_on as createdOn,
        cast(rtcata.created_by as text) as createdByText
      FROM relation_type_component_attribute_type_assignment rtcata
      inner join relation_type_component rtc on rtc.relation_type_component_id = rtcata.relation_type_component_id and rtc.deleted_flag = false
      inner join attribute_type at on rtcata.attribute_type_id = at.attribute_type_id and at.deleted_flag = false
      left join count_select cs on cs.attribute_type_id = rtcata.attribute_type_id and cs.relation_type_component_id = rtcata.relation_type_component_id
      WHERE
        (cast(:attributeTypeId as uuid) is null or rtcata.attribute_type_id = :attributeTypeId) and
        (cast(:relationTypeComponentId as uuid) is null or rtcata.relation_type_component_id = :relationTypeComponentId) and
        rtcata.deleted_flag = false
    """,
    countQuery = """
      SELECT count(*)
      FROM relation_type_component_attribute_type_assignment rtcata
      inner join relation_type_component rtc on rtc.relation_type_component_id = rtcata.relation_type_component_id and rtc.deleted_flag = false
      inner join attribute_type at on rtcata.attribute_type_id = at.attribute_type_id and at.deleted_flag = false
      WHERE
        (cast(:attributeTypeId as uuid) is null or rtcata.attribute_type_id = :attributeTypeId) and
        (cast(:relationTypeComponentId as uuid) is null or rtcata.relation_type_component_id = :relationTypeComponentId) and
        rtcata.deleted_flag = false
    """, nativeQuery = true)
  Page<RelationTypeComponentAttributeTypeAssignmentUsageCount> findAllWithUsageCountPageable (
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("relationTypeComponentId") UUID relationTypeComponentId,
    Pageable pageable
  );
}
