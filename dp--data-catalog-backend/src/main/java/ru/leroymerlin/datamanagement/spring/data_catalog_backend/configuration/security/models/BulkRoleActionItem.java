package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BulkRoleActionItem {
  private List<RoleActionResponse> allowedUserRoleActions;

  private List<RoleActionResponse> deniedUserRoleActions;

  private List<RoleActionResponse> allowedGroupRoleActions;

  private List<RoleActionResponse> deniedGroupRoleActions;

  public boolean hasAllowed () {
    return !allowedUserRoleActions.isEmpty() || !allowedGroupRoleActions.isEmpty();
  }
}
