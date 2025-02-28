package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RoleAction;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionTypeName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions.RoleActionNotFoundException;

@Service
public class RoleActionsDAO {
  @Autowired
  protected RoleActionRepository roleActionRepository;

  public RoleAction findRoleActionById (UUID roleActionId) throws RoleActionNotFoundException {
    Optional<RoleAction> roleAction = roleActionRepository.findById(roleActionId);

    if (roleAction.isEmpty()) {
      throw new RoleActionNotFoundException();
    }

    if (roleAction.get().getIsDeleted()) {
      throw new RoleActionNotFoundException();
    }

    return roleAction.get();
  }


  public List<RoleActionResponse> findAllByUserIdActionTypesAndEntityName (
    boolean byUser,
    UUID userId,
    List<ActionTypeName> actionTypeNames,
    List<EntityNameType> entityTypeNames,
    List<UUID> roleIds,
    List<UUID> assetTypeIds,
    List<UUID> attributeTypeIds,
    List<UUID> relationTypeIds
  ) {
    List<String> actionTypeNamesList = actionTypeNames.stream().map(Enum::toString).toList();
    List<String> entityTypeNamesString = entityTypeNames.stream()
      .filter(Objects::nonNull)
      .map(Enum::toString)
      .toList();

    if (byUser) {
      return roleActionRepository.findAllByUserIdActionTypesAndEntityName(
        userId,
        actionTypeNamesList,
        entityTypeNamesString,
        roleIds.size(),
        roleIds,
        assetTypeIds.size(),
        assetTypeIds,
        attributeTypeIds.size(),
        attributeTypeIds,
        relationTypeIds.size(),
        relationTypeIds
      );
    }

    return roleActionRepository.findAllByUserGroupsActionTypesAndEntityName(
      userId,
      actionTypeNamesList,
      entityTypeNamesString,
      roleIds.size(),
      roleIds,
      assetTypeIds.size(),
      assetTypeIds,
      attributeTypeIds.size(),
      attributeTypeIds,
      relationTypeIds.size(),
      relationTypeIds
    );
  }

  public List<RoleActionResponse> findAllByUserIdAndAssetIdResponsibilities (
    boolean byUser,
    UUID userId,
    List<UUID> assetIds,
    List<ActionTypeName> actionTypeNames,
    EntityNameType entityNameType,
    List<UUID> roleIds,
    List<UUID> assetTypeIds,
    List<UUID> attributeTypeIds,
    List<UUID> relationTypeIds
  ) {
    List<String> actionTypeNamesList = actionTypeNames.stream().map(Enum::toString).toList();
    String entityNameTypeString = entityNameType != null ? entityNameType.toString() : null;

    if (byUser) {
      return roleActionRepository.findAllByUserIdAndAssetIdResponsibilities(
        userId,
        assetIds,
        actionTypeNamesList,
        entityNameTypeString,
        roleIds.size(),
        roleIds,
        assetTypeIds.size(),
        assetTypeIds,
        attributeTypeIds.size(),
        attributeTypeIds,
        relationTypeIds.size(),
        relationTypeIds
      );
    }

    return roleActionRepository.findAllByUserGroupsAndAssetIdResponsibilities(
      userId,
      assetIds,
      actionTypeNamesList,
      entityNameTypeString,
      roleIds.size(),
      roleIds,
      assetTypeIds.size(),
      assetTypeIds,
      attributeTypeIds.size(),
      attributeTypeIds,
      relationTypeIds.size(),
      relationTypeIds
    );
  }

  public void deleteAllByParams (
    UUID roleId,
    UUID assetTypeId,
    UUID attributeTypeId,
    UUID relationTypeId,
    User user
  ) {
    roleActionRepository.deleteByParams(roleId, assetTypeId, attributeTypeId, relationTypeId, user.getUserId());
  }

  public List<RoleAction> findAllByRoleIdsWithJoinedTables (List<UUID> roleIds) {
    return roleActionRepository.findAllByRoleIdsWithJoinedTables(roleIds);
  }
}
