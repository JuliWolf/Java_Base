package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.models.RelationTypeComponentWithRelationType;

public interface RelationTypeComponentRepository extends JpaRepository<RelationTypeComponent, UUID> {

  @Query(value = """
    SELECT rtc FROM RelationTypeComponent rtc
      left join fetch rtc.language l
      left join fetch rtc.createdBy cb
    WHERE rtc.relationTypeComponentId = :relationTypeComponentId
  """)
  Optional<RelationTypeComponent> findByIdWithJoinedTables (
    @Param("relationTypeComponentId") UUID relationTypeComponentId
  );

  @Query(value= """
    SELECT rtc FROM RelationTypeComponent rtc
    WHERE
      rtc.relationTypeComponentId in :relationTypeComponentIds and
      rtc.relationType.relationTypeId = :relationTypeId and
      rtc.isDeleted = false
  """)
  List<RelationTypeComponent> findAllByComponentIdsAndRelationTypeId (
    @Param("relationTypeComponentIds") List<UUID> relationTypeComponentIds,
    @Param("relationTypeId") UUID relationTypeId
  );

  @Query(value= """
    SELECT rtc FROM RelationTypeComponent rtc
    WHERE
      rtc.relationType.relationTypeId in :relationTypeIds and
      rtc.isDeleted = false
  """)
  List<RelationTypeComponent> findAllRelationTypeComponentsByRelationTypeIds (
    @Param("relationTypeIds") List<UUID> relationTypeIds
  );

  @Query(value= """
    SELECT count(rtc)
    FROM RelationTypeComponent rtc
    WHERE
      rtc.relationType.relationTypeId = :relationTypeId and
      rtc.isDeleted = false
  """)
  Integer countRelationTypeComponentByRelationTypeId (
    @Param("relationTypeId") UUID relationTypeId
  );

  @Query(value= """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.models.RelationTypeComponentWithRelationType(
      rtc.relationTypeComponentId, rtc.relationType.relationTypeId
    )
    FROM RelationTypeComponent rtc
    INNER JOIN RelationTypeComponentAssetTypeAssignment rtcata on rtc.relationTypeComponentId = rtcata.relationTypeComponent.relationTypeComponentId
    WHERE
      rtcata.relationTypeComponentAssetTypeAssignmentId = :relationTypeComponentAssetTypeAssignmentId and
      rtc.isDeleted = false
  """)
  Optional<RelationTypeComponentWithRelationType> findRelationTypeComponentByRelationTypeComponentAssetTypeAssignmentId (
    @Param("relationTypeComponentAssetTypeAssignmentId") UUID relationTypeComponentAssetTypeAssignmentId
  );

  @Query(value= """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.models.RelationTypeComponentWithRelationType(
      rtc.relationTypeComponentId, rtc.relationType.relationTypeId
    )
    FROM RelationTypeComponent rtc
    INNER JOIN RelationTypeComponentAttributeTypeAssignment rtcata on rtc.relationTypeComponentId = rtcata.relationTypeComponent.relationTypeComponentId
    WHERE
      rtcata.relationTypeComponentAttributeTypeAssignmentId = :relationTypeComponentAttributeTypeAssignmentId and
      rtc.isDeleted = false
  """)
  Optional<RelationTypeComponentWithRelationType> findRelationTypeComponentByRelationTypeComponentAttributeTypeAssignmentId (
    @Param("relationTypeComponentAttributeTypeAssignmentId") UUID relationTypeComponentAttributeTypeAssignmentId
  );

  @Query(value= """
    SELECT rtc FROM RelationTypeComponent rtc
    WHERE
      rtc.relationTypeComponentId in :relationTypeComponentIds and
      rtc.isDeleted = false
  """)
  List<RelationTypeComponent> findAllByRelationTypeComponentIds (
    @Param("relationTypeComponentIds") List<UUID> relationTypeComponentIds
  );
}
