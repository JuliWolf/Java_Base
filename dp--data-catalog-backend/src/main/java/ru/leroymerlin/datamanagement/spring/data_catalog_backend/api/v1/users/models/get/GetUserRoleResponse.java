package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GetUserRoleResponse implements Response {
  private UUID role_id;

  private String role_name;

  private Integer count;
}
