package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles;

import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.RoleResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.get.GetRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PatchRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleResponse;

public interface RolesService {
  PostRoleResponse createRole (PostRoleRequest postRoleRequest, User user) throws DataIntegrityViolationException;

  GetRolesResponse getRoleByParams (String name, String description, Integer pageNumber, Integer pageSize);

  RoleResponse getRoleById (UUID roleId) throws RoleNotFoundException;

  PostRoleResponse updateRole (UUID roleId, PatchRoleRequest roleRequest, User user) throws RoleNotFoundException;

  void deleteRoleById (UUID roleId, User user) throws RoleNotFoundException;
}
