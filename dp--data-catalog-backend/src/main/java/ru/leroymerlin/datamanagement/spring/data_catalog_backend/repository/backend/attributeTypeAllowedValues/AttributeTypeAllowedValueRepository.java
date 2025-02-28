package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.models.AttributeTypeAllowedValueWithAttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeTypeAllowedValue;

/**
 * @author JuliWolf
 */
public interface AttributeTypeAllowedValueRepository extends JpaRepository<AttributeTypeAllowedValue, UUID> {
  @Query(value = """
    SELECT count(atav.valueId)
    FROM AttributeTypeAllowedValue atav
    WHERE
      atav.attributeType.attributeTypeId = :attributeTypeId and
      atav.isDeleted = false
  """)
  Integer countAttributeTypeAllowedValuesByAttributeTypeId (
    @Param("attributeTypeId") UUID attributeTypeId
  );

  @Query(value = """
    SELECT count(atav.valueId) > 0
    FROM AttributeTypeAllowedValue atav
    WHERE
      atav.attributeType.attributeTypeId = :attributeTypeId and
      atav.value = :value and
      atav.isDeleted = false
  """)
  Boolean isExistsAttributeTypeAllowedValueByAttributeTypeIdAndValue (
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("value") String value
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.models.AttributeTypeAllowedValueWithAttributeType(
      atav.attributeType.attributeTypeId,
      atav.valueId, atav.value,
      atav.createdOn, atav.createdBy.userId
    )
    FROM AttributeTypeAllowedValue atav
    WHERE
      atav.attributeType.attributeTypeId in :attributeTypeIds and
      atav.isDeleted = false
  """)
  List<AttributeTypeAllowedValueWithAttributeType> findAllByAttributeTypeId (
    @Param("attributeTypeIds") List<UUID> attributeTypeIds
  );

  @Modifying
  @Query(value ="""
    UPDATE attribute_type_allowed_value atav
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE
      attribute_type_id = :attributeTypeId
  """, nativeQuery = true)
  void deleteAllByAttributeTypeId (
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("userId") UUID userId
  );
}
