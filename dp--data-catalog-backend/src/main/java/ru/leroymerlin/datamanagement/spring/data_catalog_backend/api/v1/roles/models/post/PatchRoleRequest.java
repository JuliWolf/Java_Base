package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatchRoleRequest implements Request {
  private Optional<String> role_name;

  private Optional<String> role_description;
}
