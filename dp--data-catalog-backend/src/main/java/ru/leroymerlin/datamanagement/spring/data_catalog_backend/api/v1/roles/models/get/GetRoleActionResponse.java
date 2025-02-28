package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.get;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionTypeName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class GetRoleActionResponse {
  private ActionTypeName action_type;

  private EntityNameType entity_type;

  private ActionScopeType action_scope;

  private UUID object_type_id;

  private String object_type_name;
}
