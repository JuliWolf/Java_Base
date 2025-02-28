package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post;

import java.util.ArrayList;
import java.util.List;
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
public class PostRoleActionsRequest implements Request {
  private String role_id;

  private List<PostPrivilegeRequest> privilege_allowed = new ArrayList<>();

  private List<PostPrivilegeRequest> privilege_not_allowed = new ArrayList<>();
}
