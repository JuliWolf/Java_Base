package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributesHistory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationComponentAttributeHistory;

/**
 * @author juliwolf
 */

public interface RelationComponentAttributesHistoryRepository extends JpaRepository<RelationComponentAttributeHistory, UUID> {
  @Modifying
  @Query(value = """
    UPDATE relation_component_attribute_history
    SET
     valid_to = :newValidTo
    Where
      relation_component_attribute_id = :relationComponentAttributeId and
      valid_to = :validTo
  """, nativeQuery = true)
  void updateLastRelationComponentAttributeHistory (
    @Param("newValidTo") java.sql.Timestamp newValidTo,
    @Param("relationComponentAttributeId") UUID relationComponentAttributeId,
    @Param("validTo") java.sql.Timestamp validTo
  );

  @Query(value = """
    SELECT *
    From relation_component_attribute_history
    Where
      relation_component_attribute_id = :relationComponentAttributeId and
      valid_to = :validTo
  """, nativeQuery = true)
  List<RelationComponentAttributeHistory> selectAll (
    @Param("relationComponentAttributeId") UUID relationComponentAttributeId,
    @Param("validTo") java.sql.Timestamp validTo
  );
}
