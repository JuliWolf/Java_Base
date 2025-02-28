package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class PostPrivilegeResponse implements Response {
  private UUID role_action_id;

  private UUID action_type;

  private UUID entity_type;

  private ActionScopeType action_scope;

  private UUID object_type_id;

  private Boolean owner_user_only;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
