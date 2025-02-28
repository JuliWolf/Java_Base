package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userHistory;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.UserHistory;

/**
 * @author juliwolf
 */

public interface UserHistoryRepository extends JpaRepository<UserHistory, UUID> {
  @Modifying
  @Query(value = """
    UPDATE user_history
    SET
     valid_to = :newValidTo
    Where
      user_id = :userId and
      valid_to = :validTo
  """, nativeQuery = true)
  void updateLastUserHistory (
    @Param("newValidTo") java.sql.Timestamp newValidTo,
    @Param("userId") UUID userId,
    @Param("validTo") java.sql.Timestamp validTo
  );

  @Modifying
  @Query(value = """
    UPDATE user_history
    SET
     valid_to = sub.deleted_on
    From (
      Select deleted_on
      From user
      Where user_id = :userId
    ) sub
    Where
      user_id = :userId and
      valid_to = :validTo
  """, nativeQuery = true)
  void updateLastUserHistoryByDeletedUserId (
    @Param("userId") UUID userId,
    @Param("validTo") java.sql.Timestamp validTo
  );

  @Modifying
  @Query(value = """
    INSERT INTO user_history (
      user_history_id, user_id,
      boss_k_pid, username,
      email, first_name, last_name,
      source, messenger, struct_unit_id,
      user_type, user_photo_link,
      user_work_status, source_language,
      created_on, created_by,
      last_modified_on, last_modified_by,
      deleted_flag, deleted_on, deleted_by,
      valid_from, valid_to
    )
    Select
      :userHistoryId, :userId,
      u.user_history_id, u.user_id,
      u.boss_k_pid, u.username,
      u.email, u.first_name, u.last_name,
      u.source, u.messenger, u.struct_unit_id,
      u.user_type, u.user_photo_link,
      u.user_work_status, u.source_language,
      u.created_on, u.created_by,
      u.last_modified_on, u.last_modified_by,
      u.deleted_flag, u.deleted_on, u.deleted_by,
      u.deleted_on, u.deleted_on
    From user u
    Where u.user_id = :userId
  """, nativeQuery = true)
  void createUserHistoryFromUser (
    @Param("userHistoryId") UUID userHistoryId,
    @Param("userId") UUID userId
  );
}
