package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.userGroups;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.UserGroup;

/**
 * @author JuliWolf
 */
public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
  @Modifying
  @Query(value = """
    UPDATE user_group
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :deletedBy
    WHERE
      (cast(:userId as uuid) is null or user_id = :userId) and
      (cast(:groupId as uuid) is null or group_id = :groupId)
  """, nativeQuery = true)
  void deleteByParams (
    @Param("userId") UUID userId,
    @Param("groupId") UUID groupId,
    @Param("deletedBy") UUID deletedBy
  );

  @Query(value = """
    SELECT ug FROM UserGroup ug
    left join fetch ug.user
    WHERE
      (cast(:userId as org.hibernate.type.PostgresUUIDType) is null or ug.user.userId = :userId) and
      (cast(:groupId as org.hibernate.type.PostgresUUIDType) is null or ug.group.groupId = :groupId) and
      ug.isDeleted = false
  """, countQuery = """
    SELECT count(ug.userGroupId) FROM UserGroup ug
    WHERE
      (cast(:userId as org.hibernate.type.PostgresUUIDType) is null or ug.user.userId = :userId) and
      (cast(:groupId as org.hibernate.type.PostgresUUIDType) is null or ug.group.groupId = :groupId) and
      ug.isDeleted = false
  """)
  Page<UserGroup> findAllByUserIdAndGroupIdPageable (
    @Param("userId") UUID userId,
    @Param("groupId") UUID groupId,
    Pageable pageable
  );
}
