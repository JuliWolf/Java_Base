package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get;

import java.util.List;
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
public class GetUserRolesResponse implements Response {
  private Long total;

  private List<GetUserRoleResponse> results;
}
