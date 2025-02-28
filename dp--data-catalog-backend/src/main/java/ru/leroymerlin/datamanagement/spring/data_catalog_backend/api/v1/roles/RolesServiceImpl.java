package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.role.RoleWithUsageCountResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.RoleUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.CustomViewsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.RoleActionsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.RoleResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.get.GetRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PatchRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleResponse;

/**
 * @author JuliWolf
 */
@Service
public class RolesServiceImpl extends RolesDAO implements RolesService {
  @Autowired
  private RoleActionCachingService roleActionCachingService;

  @Autowired
  private LanguageService languageService;

  @Autowired
  private RoleActionsDAO roleActionsDAO;

  @Autowired
  private GlobalResponsibilitiesDAO globalResponsibilitiesDAO;

  @Autowired
  private ResponsibilitiesDAO responsibilitiesDAO;

  @Autowired
  private AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO;

  @Autowired
  private CustomViewsDAO customViewsDAO;

  @Override
  public PostRoleResponse createRole (PostRoleRequest postRoleRequest, User user) throws DataIntegrityViolationException {
    Language ru = languageService.getLanguage("ru");

    Role role = roleRepository.save(new Role(
        postRoleRequest.getRole_name(),
        postRoleRequest.getRole_description(),
        ru,
        user
    ));

    // Clear cache if someone tried to get role with this id
    clearRoleActionCache(role.getRoleId());

    return new PostRoleResponse(
        role.getRoleId(),
        role.getRoleName(),
        role.getRoleDescription(),
        role.getLanguageName(),
        role.getCreatedOn(),
        role.getCreatedByUUID(),
        null,
        null
    );
  }

  @Override
  public GetRolesResponse getRoleByParams (
    String name,
    String description,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<RoleWithUsageCountResponse> responses = roleRepository.findAllByRoleNameAndDescriptionPageable(
      name,
      description,
      PageRequest.of(pageNumber, pageSize, Sort.by("roleName").ascending())
    );

    List<RoleResponse> rolesCollection = responses.stream().map(response -> {
      Role role = response.getRole();
      UUID lastModifiedBy = role.getModifiedBy() != null ? role.getModifiedBy().getUserId() : null;

      return new RoleResponse(
        role.getRoleId(),
        role.getRoleName(),
        role.getRoleDescription(),
        response.getUsageCount(),
        response.getResponsibilitiesCount(),
        response.getGlobalResponsibilitiesCount(),
        role.getLanguageName(),
        role.getCreatedOn(),
        role.getCreatedByUUID(),
        role.getLastModifiedOn(),
        lastModifiedBy
      );
    }).toList();

    return new GetRolesResponse(
      responses.getTotalElements(),
      pageSize,
      pageNumber,
      rolesCollection
    );
  }

  @Override
  public RoleResponse getRoleById (UUID roleId) throws RoleNotFoundException {
    Role role = findRoleById(roleId);

    RoleUsageCount roleUsageCount = roleRepository.getUsageCountByRoleId(roleId);

    UUID lastModifiedBy = role.getModifiedBy() != null ? role.getModifiedBy().getUserId() : null;

    return new RoleResponse(
      role.getRoleId(),
      role.getRoleName(),
      role.getRoleDescription(),
      roleUsageCount.getUsageCount(),
      roleUsageCount.getResponsibilitiesUsageCount(),
      roleUsageCount.getGlobalResponsibilitiesUsageCount(),
      role.getLanguageName(),
      role.getCreatedOn(),
      role.getCreatedByUUID(),
      role.getLastModifiedOn(),
      lastModifiedBy
    );
  }

  @Override
  public PostRoleResponse updateRole (UUID roleId, PatchRoleRequest roleRequest, User user) throws RoleNotFoundException {
    Role foundRole = findRoleById(roleId);

    OptionalUtils.doActionIfPresent(roleRequest.getRole_name(), roleName -> foundRole.setRoleName(roleName.orElse(foundRole.getRoleName())));
    OptionalUtils.doActionIfPresent(roleRequest.getRole_description(), roleDescription -> foundRole.setRoleDescription(roleDescription.orElse(null)));

    foundRole.setModifiedBy(user);
    foundRole.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));

    Role role = roleRepository.save(foundRole);

    return new PostRoleResponse(
      role.getRoleId(),
      role.getRoleName(),
      role.getRoleDescription(),
      role.getLanguageName(),
      role.getCreatedOn(),
      role.getCreatedByUUID(),
      role.getLastModifiedOn(),
      role.getModifiedBy().getUserId()
    );
  }

  @Override
  @Transactional
  public void deleteRoleById (UUID roleId, User user) throws RoleNotFoundException {
    Role foundRole = findRoleById(roleId);

    customViewsDAO.deleteByParams(null, roleId, user);
    roleActionsDAO.deleteAllByParams(roleId, null, null, null, user);
    responsibilitiesDAO.deleteAllByParams(null, roleId, null, null, null, null, user);
    globalResponsibilitiesDAO.deleteAllByRoleId(roleId, user);
    assetTypeCardHeaderAssignmentDAO.deleteAssetTypeCardHeaderAssignmentByParams(roleId, null, null, user);

    foundRole.setIsDeleted(true);
    foundRole.setDeletedBy(user);
    foundRole.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    roleRepository.save(foundRole);

    clearRoleActionCache(roleId);
  }

  private void clearRoleActionCache (UUID roleId) {
    roleActionCachingService.evictByRoleId(roleId);
    roleActionCachingService.evictByValueInKey(roleId.toString());
  }
}
