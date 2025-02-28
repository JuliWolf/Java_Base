package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.models.GlobalResponsibilityWithRoleAndResponsible;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.GlobalResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;

/**
 * @author JuliWolf
 */
public interface GlobalResponsibilitiesRepository extends JpaRepository<GlobalResponsibility, UUID> {

  @Modifying
  @Query(value = """
    UPDATE global_responsibility
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    WHERE
      (cast(:roleId as uuid) is null or role_id = :roleId) and
      (cast(:responsibleId as uuid) is null or
          (user_id is not null and user_id = :responsibleId) or
          (group_id is not null and group_id = :responsibleId)
      ) and
      (:responsibleType is null or responsible_type = :responsibleType)
  """, nativeQuery = true)
  void deleteByParams (
    @Param("roleId") UUID roleId,
    @Param("responsibleId") UUID responsibleId,
    @Param("responsibleType") String responsibleType,
    @Param("userId") UUID userId
  );

  @Query(value = """
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.models.GlobalResponsibilityWithRoleAndResponsible(
      gr.globalResponsibilityId, g.groupId, g.groupName,
      u.userId, u.username, gr.responsibleType,
      r.roleId, r.roleName, r.roleDescription,
      gr.createdOn, cb.userId
    )
    FROM GlobalResponsibility gr
    Left join Group g on g.groupId = gr.group.groupId
    Left Join User u on u.userId = gr.user.userId
    Left Join Role r on r.roleId = gr.role.roleId
    Left Join User cb on cb.userId = gr.createdBy.userId
    WHERE
      (cast(:roleId as org.hibernate.type.PostgresUUIDType) is null or gr.role.roleId = :roleId) and
      (cast(:responsibleId as org.hibernate.type.PostgresUUIDType) is null or
          (gr.user is not null and gr.user.userId = :responsibleId) or
          (gr.group is not null and gr.group.groupId = :responsibleId)
      ) and
      (:responsibleType is null or gr.responsibleType = :responsibleType) and
      gr.isDeleted = false
  """, countQuery = """
    SELECT count(gr.globalResponsibilityId) FROM GlobalResponsibility gr
    WHERE
      (cast(:roleId as org.hibernate.type.PostgresUUIDType) is null or gr.role.roleId = :roleId) and
      (cast(:responsibleId as org.hibernate.type.PostgresUUIDType) is null or
          (gr.user is not null and gr.user.userId = :responsibleId) or
          (gr.group is not null and gr.group.groupId = :responsibleId)
      ) and
      (:responsibleType is null or gr.responsibleType = :responsibleType) and
      gr.isDeleted = false
  """)
  Page<GlobalResponsibilityWithRoleAndResponsible> findAllByRoleIdResponsibleIdAndResponsibilityTypePageable (
    @Param("roleId") UUID roleId,
    @Param("responsibleId") UUID responsibleId,
    @Param("responsibleType") ResponsibleType responsibleType,
    Pageable pageable
  );

  @Query("""
    SELECT new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.models.GlobalResponsibilityWithRoleAndResponsible(
      gr.globalResponsibilityId, g.groupId, g.groupName,
      u.userId, u.username, gr.responsibleType,
      r.roleId, r.roleName, r.roleDescription,
      gr.createdOn, cb.userId
    )
    FROM GlobalResponsibility gr
    Left join Group g on g.groupId = gr.group.groupId
    Left Join User u on u.userId = gr.user.userId
    Left Join Role r on r.roleId = gr.role.roleId
    Left Join User cb on cb.userId = gr.createdBy.userId
    WHERE
      gr.globalResponsibilityId = :globalResponsibilityId and
      gr.isDeleted = false
  """)
  Optional<GlobalResponsibilityWithRoleAndResponsible> findGlobalResponsibilityById (
    @Param("globalResponsibilityId") UUID globalResponsibilityId
  );
}
