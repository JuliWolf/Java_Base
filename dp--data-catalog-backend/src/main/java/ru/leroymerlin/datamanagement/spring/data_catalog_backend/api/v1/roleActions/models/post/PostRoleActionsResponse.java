package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostRoleActionsResponse implements Response {
  private UUID role_id;

  private List<PostPrivilegeResponse> privilege_allowed = new ArrayList<>();

  private List<PostPrivilegeResponse> privilege_not_allowed = new ArrayList<>();

  public PostRoleActionsResponse(UUID role_id) {
    this.role_id = role_id;
  }
}
