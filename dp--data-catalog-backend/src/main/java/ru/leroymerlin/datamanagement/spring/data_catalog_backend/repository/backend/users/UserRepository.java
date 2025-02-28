package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.models.UserRoleResponsibilityCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.models.UserWithLanguage;

/**
 * @author JuliWolf
 */
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> getUserByUsernameAndIsDeletedFalse(String userName);

  Optional<User> getUserByUserIdAndIsDeletedFalse(UUID userId);

  @Query(value= """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.models.UserWithLanguage(
      us.userId, us.username, us.email, us.firstName, us.lastName,
      us.source, l.language, us.createdOn, us.createdBy.userId,
      us.lastModifiedOn, us.modifiedBy.userId, us.lastLoginTime
    )
    FROM User us
      left join us.language l
      left join us.createdBy cb
      left join us.modifiedBy lmb
    WHERE
      CASE WHEN :isSearchByAll = true THEN (
        (
          (:username is null or lower(us.username) LIKE '%' || lower(:username) || '%') or
          (:firstName is null or lower(us.firstName) LIKE '%' || lower(:firstName) || '%') or
          (:lastName is null or lower(us.lastName) LIKE '%' || lower(:lastName) || '%') or
          (:email is null or lower(us.email) LIKE '%' || lower(:email) || '%') or
          (concat(lower(us.firstName), ' ', lower(us.lastName)) LIKE '%' || lower(:name) || '%') or
          (concat(lower(us.lastName), ' ', lower(us.firstName)) LIKE '%' || lower(:name) || '%')
        ) and us.isDeleted = false
      ) ELSE (
        (:username is null or lower(us.username) LIKE '%' || lower(:username) || '%') and
        (:firstName is null or lower(us.firstName) LIKE '%' || lower(:firstName) || '%') and
        (:lastName is null or lower(us.lastName) LIKE '%' || lower(:lastName) || '%') and
        (:email is null or lower(us.email) LIKE '%' || lower(:email) || '%') and
        us.isDeleted = false
      ) END
  """, countQuery = """
    SELECT count(us) FROM User us
     WHERE
      CASE WHEN :isSearchByAll = true THEN (
        (
          (:username is null or lower(us.username) LIKE '%' || lower(:username) || '%') or
          (:firstName is null or lower(us.firstName) LIKE '%' || lower(:firstName) || '%') or
          (:lastName is null or lower(us.lastName) LIKE '%' || lower(:lastName) || '%') or
          (:email is null or lower(us.email) LIKE '%' || lower(:email) || '%') or
          (concat(lower(us.firstName), ' ', lower(us.lastName)) LIKE '%' || lower(:name) || '%') or
          (concat(lower(us.lastName), ' ', lower(us.firstName)) LIKE '%' || lower(:name) || '%')
        ) and us.isDeleted = false
      ) ELSE (
        (:username is null or lower(us.username) LIKE '%' || lower(:username) || '%') and
        (:firstName is null or lower(us.firstName) LIKE '%' || lower(:firstName) || '%') and
        (:lastName is null or lower(us.lastName) LIKE '%' || lower(:lastName) || '%') and
        (:email is null or lower(us.email) LIKE '%' || lower(:email) || '%') and
        us.isDeleted = false
      ) END
  """)
  Page<UserWithLanguage> findAllByParamsPageable (
    @Param("name") String name,
    @Param("username") String username,
    @Param("firstName") String firstName,
    @Param("lastName") String lastName,
    @Param("email") String email,
    @Param("isSearchByAll") boolean isSearchByAll,
    Pageable pageable
  );

  @Query("""
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.models.UserWithLanguage(
      us.userId, us.username, us.email, us.firstName, us.lastName,
      us.source, l.language, us.createdOn, us.createdBy.userId,
      us.lastModifiedOn, us.modifiedBy.userId, us.lastLoginTime
    )
    FROM User us
      left join us.language l
      left join us.createdBy cb
      left join us.modifiedBy lmb
    WHERE
      us.userId = :userId and
      us.isDeleted = false
  """)
  Optional<UserWithLanguage> findUserById (
    @Param("userId") UUID userId
  );

  @Query(value = """
    select
      cast(ur.role_id as text) as roleIdText,
      cast(role_name as text) as roleName,
      role_usage_count as roleUsageCount
    from role
    inner join (
      select role_id, count(*) as role_usage_count
      from responsibility r
      where
        user_id = :userId and
        deleted_flag = false
        group by role_id
    ) ur on role.role_id = ur.role_id
  """, nativeQuery = true)
  List<UserRoleResponsibilityCount> findUserRoleResponsibilitiesCount (
    @Param("userId") UUID userId
  );

  @Query("""
    Select u
    FROM User u
    where
      u.userId in :userIds and
      u.isDeleted = false
  """)
  List<User> findUsersByUserIds (
    @Param("userIds") List<UUID> userIds
  );
}
