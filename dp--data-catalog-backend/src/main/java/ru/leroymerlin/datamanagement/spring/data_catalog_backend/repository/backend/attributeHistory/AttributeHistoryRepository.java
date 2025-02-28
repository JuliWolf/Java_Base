package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeHistory;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeHistory;

/**
 * @author juliwolf
 */

public interface AttributeHistoryRepository extends JpaRepository<AttributeHistory, UUID> {
  @Modifying
  @Query(value = """
    UPDATE attribute_history
    SET
     valid_to = :newValidTo
    Where
      attribute_id = :attributeId and
      valid_to = :validTo
  """, nativeQuery = true)
  void updateLastAttributeHierarchy (
    @Param("newValidTo") java.sql.Timestamp newValidTo,
    @Param("attributeId") UUID attributeId,
    @Param("validTo") java.sql.Timestamp validTo
  );

  @Modifying
  @Query(value = """
    UPDATE attribute_history
    SET
     valid_to = sub.deleted_on
    From (
      Select deleted_on
      From attribute
      Where attribute_id = :attributeId
    ) sub
    Where
      attribute_id = :attributeId and
      valid_to = :validTo
  """, nativeQuery = true)
  void updateLastAttributeHistoryByDeletedAttributeId (
    @Param("attributeId") UUID attributeId,
    @Param("validTo") java.sql.Timestamp validTo
  );

  @Modifying
  @Query(value = """
    INSERT INTO attribute_history (
      attribute_history_id, attribute_id,
      attribute_type_id, asset_id,
      value, integer_flag,
      value_numeric, value_bool, value_datetime,
      source_language, created_on, created_by,
      last_modified_on, last_modified_by,
      deleted_flag, deleted_on, deleted_by,
      valid_from, valid_to
    )
    Select
      :attributeHistoryId, :attributeId,
      a.attribute_type_id, a.asset_id,
      a.value, a.integer_flag,
      a.value_numeric, a.value_bool, a.value_datetime,
      a.source_language, a.created_on, a.created_by,
      a.last_modified_on, a.last_modified_by,
      a.deleted_flag, a.deleted_on, a.deleted_by,
      a.deleted_on, a.deleted_on
    From attribute a
    Where a.attribute_id = :attributeId
  """, nativeQuery = true)
  void createAttributeHistoryFromAttribute (
    @Param("attributeHistoryId") UUID attributeHistoryId,
    @Param("attributeId") UUID attributeId
  );
}
