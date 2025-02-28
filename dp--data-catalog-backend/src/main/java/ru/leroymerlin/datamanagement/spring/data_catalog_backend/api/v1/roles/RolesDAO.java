package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.UserRole;

@Service
public class RolesDAO {
  public final UUID DEFAULT_ROLE_ID = UUID.fromString("2c057d40-4f72-4eb3-9a5e-276a9a98e1f8");

  @Autowired
  protected RoleRepository roleRepository;

  public Role findRoleById(UUID roleId) throws RoleNotFoundException {
    Optional<Role> role = roleRepository.findById(roleId);

    if (role.isEmpty()) {
      throw new RoleNotFoundException();
    }

    if (role.get().getIsDeleted()) {
      throw new RoleNotFoundException();
    }

    return role.get();
  }

  public List<Role> findAllByRoleIds (List<UUID> roleIds) {
    return roleRepository.findAllByRoleIds(roleIds);
  }

  public List<UserRole> findAllByUserId (UUID userId, Boolean isGroup) {
    if (isGroup) {
      return roleRepository.findAllByUserGroup(userId);
    }

    return roleRepository.findAllByUserId(userId);
  }
}
