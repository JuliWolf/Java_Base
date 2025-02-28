package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.role.RoleWithUsageCountResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.RoleUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.UserRole;

/**
 * @author JuliWolf
 */
public interface RoleRepository extends JpaRepository<Role, UUID>{

  @Query(value = """
    SELECT r as role, COALESCE(count(distinct resp), 0) as responsibilitiesCount, COALESCE(count(distinct gresp), 0) as globalResponsibilitiesCount
    FROM Role r
    left join r.responsibilities resp on resp.role.roleId = r.roleId and resp.isDeleted = false
    left join r.globalResponsibilities gresp on gresp.role.roleId = r.roleId and gresp.isDeleted = false
    WHERE
      (:roleName is null or lower(r.roleName) LIKE '%' || lower(:roleName) || '%') and
      (:roleDescription is null or lower(r.roleDescription) LIKE '%' || lower(:roleDescription) || '%') and
      r.isDeleted = false
      GROUP BY r.roleId
  """, countQuery = """
    SELECT count(r) FROM Role r
    WHERE
      (:roleName is null or lower(r.roleName) LIKE '%' || lower(:roleName) || '%') and
      (:roleDescription is null or lower(r.roleDescription) LIKE '%' || lower(:roleDescription) || '%') and
      r.isDeleted = false
  """)
  Page<RoleWithUsageCountResponse> findAllByRoleNameAndDescriptionPageable (
    @Param("roleName") String roleName,
    @Param("roleDescription") String roleDescription,
    Pageable pageable
  );

  @Query(value= """
    SELECT
      COALESCE(count(distinct resp), 0) as responsibilitiesUsageCount,
      COALESCE(count(distinct gresp), 0) as globalResponsibilitiesUsageCount
    FROM Role r
    left join r.responsibilities resp on resp.role.roleId = r.roleId and resp.isDeleted = false
    left join r.globalResponsibilities gresp on gresp.role.roleId = r.roleId and gresp.isDeleted = false
    WHERE
      r.roleId = :roleId and
      r.isDeleted = false
      GROUP BY r.roleId
  """)
  RoleUsageCount getUsageCountByRoleId (
    @Param("roleId") UUID roleId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.UserRole(
      r.roleId, r.roleName
    )
    FROM Role r
    inner join GlobalResponsibility gr on gr.role.roleId = r.roleId and gr.responsibleType = 'USER'
    WHERE
      gr.user.userId = :userId and
      r.isDeleted = false
  """)
  List<UserRole> findAllByUserId (
    @Param("userId") UUID userId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.UserRole(
      r.roleId, r.roleName
    )
    FROM Role r
    inner join GlobalResponsibility gr on gr.role.roleId = r.roleId and gr.responsibleType = 'GROUP'
    inner join Group g on g.groupId = gr.group.groupId
    inner join UserGroup us on us.group.groupId = g.groupId
    WHERE
      us.user.userId = :userId and
      r.isDeleted = false
  """)
  List<UserRole> findAllByUserGroup (
    @Param("userId") UUID userId
  );

  @Query(value = """
    SELECT r
    FROM Role r
    WHERE
      r.roleId in :roleIds and
      r.isDeleted = false
  """)
  List<Role> findAllByRoleIds (
    @Param("roleIds") List<UUID> roleIds
  );
}
