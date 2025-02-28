package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationType;

public interface RelationTypeRepository extends JpaRepository<RelationType, UUID> {
  @Query(value = """
    SELECT rt FROM RelationType rt
      left join fetch rt.language l
      left join fetch rt.createdBy cb
    WHERE rt.relationTypeId = :relationTypeId
  """)
  Optional<RelationType> findByIdWithJoinedTables (
    @Param("relationTypeId") UUID relationTypeId
  );

  @Query("""
    SELECT rt
    FROM RelationType rt
      left join rt.language l
      left join rt.createdBy cb
    WHERE rt.relationTypeId in :relationTypeIds
  """)
  List<RelationType> findAllByRelationTypeIds (
    @Param("relationTypeIds") List<UUID> relationTypeIds
  );

  @Query(value = """
    SELECT rt
    FROM RelationType rt
      left join fetch rt.language l
      left join fetch rt.createdBy cb
    WHERE
      (:relationTypeName is null or lower(rt.relationTypeName) LIKE '%' || lower(:relationTypeName) || '%') and
      (:componentNumber is null or rt.componentNumber = :componentNumber) and
      (:hierarchyFlag is null or rt.hierarchyFlag = :hierarchyFlag) and
      (:responsibilityInheritanceFlag is null or rt.responsibilityInheritanceFlag = :responsibilityInheritanceFlag) and
      (:selfRelatedFlag is null or rt.selfRelatedFlag = :selfRelatedFlag) and
      (:uniquenessFlag is null or rt.uniquenessFlag = :uniquenessFlag) and
      rt.isDeleted = false
  """, countQuery = """
    SELECT count(rt.relationTypeId)
    FROM RelationType rt
    WHERE
      (:relationTypeName is null or lower(rt.relationTypeName) LIKE '%' || lower(:relationTypeName) || '%') and
      (:componentNumber is null or rt.componentNumber = :componentNumber) and
      (:hierarchyFlag is null or rt.hierarchyFlag = :hierarchyFlag) and
      (:responsibilityInheritanceFlag is null or rt.responsibilityInheritanceFlag = :responsibilityInheritanceFlag) and
      (:selfRelatedFlag is null or rt.selfRelatedFlag = :selfRelatedFlag) and
      (:uniquenessFlag is null or rt.uniquenessFlag = :uniquenessFlag) and
      rt.isDeleted = false
  """)
  Page<RelationType> findAllByParamsWithJoinedTablesPageable (
    @Param("relationTypeName") String relationTypeName,
    @Param("componentNumber") Integer componentNumber,
    @Param("hierarchyFlag") Boolean hierarchyFlag,
    @Param("responsibilityInheritanceFlag") Boolean responsibilityInheritanceFlag,
    @Param("selfRelatedFlag") Boolean selfRelatedFlag,
    @Param("uniquenessFlag") Boolean uniquenessFlag,
    Pageable pageable
  );


  @Query(value = """
    SELECT distinct rt.*
    FROM relation_type rt
      left join "user" on rt.created_by = "user".user_id
      inner join (
        SELECT relation_type_id from relation_type_component rtc
        inner join (
            select asset_type_id, relation_type_component_id from relation_type_component_asset_type_assignment
            where asset_type_id = :allowedAssetTypeId and
                  deleted_flag = false
        ) rtcaat on rtcaat.relation_type_component_id = rtc.relation_type_component_id
      ) rtf on rt.relation_type_id = rtf.relation_type_id
    WHERE
      (:relationTypeName is null or lower(rt.relation_type_name) LIKE '%' || lower(:relationTypeName) || '%') and
      (:componentNumber is null or rt.component_number = :componentNumber) and
      (:hierarchyFlag is null or rt.hierarchy_flag = :hierarchyFlag) and
      (:responsibilityInheritanceFlag is null or rt.responsibility_inheritance_flag = :responsibilityInheritanceFlag) and
      (:selfRelatedFlag is null or rt.self_related_flag = :selfRelatedFlag) and
      (:uniquenessFlag is null or rt.uniqueness_flag = :uniquenessFlag) and
      rt.deleted_flag = false
  """, countQuery = """
    SELECT count(distinct rt.relation_type_id)
    FROM relation_type rt
      left join language on rt.source_language = language.language_id
      left join "user" on rt.created_by = "user".user_id
      inner join (
        SELECT relation_type_id from relation_type_component rtc
          inner join (
            select asset_type_id, relation_type_component_id from relation_type_component_asset_type_assignment
            where asset_type_id = :allowedAssetTypeId and
                  deleted_flag = false
          ) rtcaat on rtcaat.relation_type_component_id = rtc.relation_type_component_id
      ) rtf on rt.relation_type_id = rtf.relation_type_id
    WHERE
      (:relationTypeName is null or lower(rt.relation_type_name) LIKE '%' || lower(:relationTypeName) || '%') and
      (:componentNumber is null or rt.component_number = :componentNumber) and
      (:hierarchyFlag is null or rt.hierarchy_flag = :hierarchyFlag) and
      (:responsibilityInheritanceFlag is null or rt.responsibility_inheritance_flag = :responsibilityInheritanceFlag) and
      (:selfRelatedFlag is null or rt.self_related_flag = :selfRelatedFlag) and
      (:uniquenessFlag is null or rt.uniqueness_flag = :uniquenessFlag) and
      rt.deleted_flag = false
  """, nativeQuery = true)
  Page<RelationType> findAllByParamsAndAllowedAssetTypePageable (
    @Param("relationTypeName") String relationTypeName,
    @Param("componentNumber") Integer componentNumber,
    @Param("hierarchyFlag") Boolean hierarchyFlag,
    @Param("responsibilityInheritanceFlag") Boolean responsibilityInheritanceFlag,
    @Param("allowedAssetTypeId") UUID allowedAssetType,
    @Param("selfRelatedFlag") Boolean selfRelatedFlag,
    @Param("uniquenessFlag") Boolean uniquenessFlag,
    Pageable pageable
  );
}
