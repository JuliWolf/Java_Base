package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post;

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
public class PostRoleRequest implements Request {
  private String role_name;

  private String role_description;
}
