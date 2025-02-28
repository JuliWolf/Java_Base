package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.get;

import java.util.List;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.RoleResponse;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class GetRolesResponse implements Response {
  long total;

  int page_size;

  int page_number;

  private List<RoleResponse> results;
}
