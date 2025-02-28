package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models;

import java.util.List;
import java.util.Map;
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
public class BulkRoleActionByValue {
  private Map<String, List<RoleActionResponse>> allowedUserRoleActions;

  private Map<String, List<RoleActionResponse>> deniedUserRoleActions;

  private Map<String, List<RoleActionResponse>> allowedGroupRoleActions;

  private Map<String, List<RoleActionResponse>> deniedGroupRoleActions;
}
