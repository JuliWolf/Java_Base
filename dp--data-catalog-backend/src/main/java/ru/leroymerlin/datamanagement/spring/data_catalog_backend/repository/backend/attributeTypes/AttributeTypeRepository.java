package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.models.AttributeTypeUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;

/**
 * @author JuliWolf
 */
public interface AttributeTypeRepository extends JpaRepository<AttributeType, UUID> {

  boolean existsByAttributeTypeIdAndIsDeletedFalse(UUID attributeTypeId);

  @Query(value = """
    SELECT at FROM AttributeType at
    WHERE
      (:attributeTypeName is null or lower(at.attributeTypeName) LIKE '%' || lower(:attributeTypeName) || '%') and
      (:attributeTypeDescription is null or lower(at.attributeTypeDescription) LIKE '%' || lower(:attributeTypeDescription) || '%') and
      (:attributeTypeKind is null or at.attributeKindType = :attributeTypeKind) and
      at.isDeleted = false
  """, countQuery = """
    SELECT count(at.attributeTypeId) FROM AttributeType at
    WHERE
      (:attributeTypeName is null or lower(at.attributeTypeName) LIKE '%' || lower(:attributeTypeName) || '%') and
      (:attributeTypeDescription is null or lower(at.attributeTypeDescription) LIKE '%' || lower(:attributeTypeDescription) || '%') and
      (:attributeTypeKind is null or at.attributeKindType = :attributeTypeKind) and
      at.isDeleted = false
  """)
  Page<AttributeType> findAllByParamsPageable (
    @Param("attributeTypeName") String attributeTypeName,
    @Param("attributeTypeDescription") String attributeTypeDescription,
    @Param("attributeTypeKind") AttributeKindType attributeTypeKind,
    Pageable pageable
  );

  @Query(value = """
    SELECT at FROM AttributeType at
    left join fetch at.createdBy
    left join fetch at.allowedValues
    left join fetch at.language
    WHERE at.attributeTypeId = :attributeTypeId
  """)
  Optional<AttributeType> findAttributeTypeByAttributeTypeIdWithJoinedTables (
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value= """
    SELECT at
    FROM AttributeType at
    Where
      at.attributeTypeId in :attributeTypeIds and
      at.isDeleted = false
  """)
  List<AttributeType> findAttributeTypesByIds (
    @Param("attributeTypeIds") List<UUID> attributeTypeIds
  );

  @Query(value= """
    SELECT at
    FROM AttributeType at
    Inner Join Attribute a on a.attributeType.attributeTypeId = at.attributeTypeId
    Where
      a.attributeId in :attributeIds and
      at.isDeleted = false
  """)
  List<AttributeType> findAttributeTypesByAttributeIds (
    @Param("attributeIds") List<UUID> attributeIds
  );

  @Query(value= """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.models.AttributeTypeUsageCount(
      a.attributeType.attributeTypeId, count(*)
    )
    FROM Attribute a
    Where
      a.attributeType.attributeTypeId in :attributeTypeIds and
      a.isDeleted = false
    group by a.attributeType.attributeTypeId
  """)
  List<AttributeTypeUsageCount> countAttributeTypesUsage (
    @Param("attributeTypeIds") List<UUID> attributeTypeIds
  );
}
