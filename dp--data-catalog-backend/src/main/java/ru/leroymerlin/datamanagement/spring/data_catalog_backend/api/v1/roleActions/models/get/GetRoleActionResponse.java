package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionTypeName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetRoleActionResponse {
  private UUID role_action_id;

  private UUID role_id;

  private String role_name;

  private UUID entity_type_id;

  private EntityNameType entity_type_name;

  private UUID object_type_id;

  private String object_type_name;

  private UUID action_type_id;

  private ActionTypeName action_type_name;

  private ActionScopeType action_scope;

  private PermissionType permission_type;

  private Boolean owner_user_only;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
