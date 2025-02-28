package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributesHistory;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationAttributeHistory;

/**
 * @author juliwolf
 */

public interface RelationAttributesHistoryRepository extends JpaRepository<RelationAttributeHistory, UUID> {
  @Modifying
  @Query(value = """
    UPDATE relation_attribute_history
    SET
     valid_to = :newValidTo
    Where
      relation_attribute_id = :relationAttributeId and
      valid_to = :validTo
  """, nativeQuery = true)
  void updateLastRelationAttributeHistory (
    @Param("newValidTo") java.sql.Timestamp newValidTo,
    @Param("relationAttributeId") UUID relationAttributeId,
    @Param("validTo") java.sql.Timestamp validTo
  );
}
