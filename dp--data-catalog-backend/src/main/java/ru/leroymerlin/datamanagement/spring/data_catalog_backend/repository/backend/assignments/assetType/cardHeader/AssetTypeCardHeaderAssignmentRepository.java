package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.models.AssetTypeCardHeaderAssignmentResponsible;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeCardHeaderAssignment;

/**
 * @author juliwolf
 */

public interface AssetTypeCardHeaderAssignmentRepository extends JpaRepository<AssetTypeCardHeaderAssignment, UUID> {
  @Modifying
  @Query(value = """
    UPDATE asset_type_card_header_assignment
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      (cast(:roleId  as uuid) is null OR owner_field_role_id = :roleId) and
      (cast(:assetTypeId  as uuid) is null OR asset_type_id = :assetTypeId) and
      (cast(:attributeTypeId  as uuid) is null OR description_field_attribute_type_id = :attributeTypeId)
  """, nativeQuery = true)
  void deleteAssetTypeCardHeaderAssignmentByParams (
    @Param("roleId") UUID roleId,
    @Param("assetTypeId") UUID assetTypeId,
    @Param("attributeTypeId") UUID attributeTypeId,
    @Param("userId") UUID userId
  );


  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.models.AssetTypeCardHeaderAssignmentResponsible(
      resp.responsibilityId, resp.responsibleType,
      u.userId, u.username, u.firstName, u.lastName,
      g.groupId, g.groupName
    )
    From Asset a
    Inner Join AssetType at on a.assetType.assetTypeId = at.assetTypeId
    left Join AssetTypeCardHeaderAssignment atcha on at.assetTypeId = atcha.assetType.assetTypeId
    Left Join Role ofr on ofr.roleId = atcha.ownerFieldRole.roleId and ofr.isDeleted = false
    left join Responsibility resp on resp.role.roleId = coalesce(ofr.roleId, :staticRoleId) and resp.asset.assetId = :assetId and resp.isDeleted = false
    left join Group g on g.groupId = resp.group.groupId
    Left join User u on u.userId = resp.user.userId
    Where
      a.assetId = :assetId and
      a.isDeleted = false
  """)
  List<AssetTypeCardHeaderAssignmentResponsible> findAllAssetCardHeaderResponsibleByAssetId (
    @Param("assetId") UUID assetId,
    @Param("staticRoleId") UUID staticRoleId
  );
}
