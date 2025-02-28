package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post;

import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class PostPrivilegeRequest implements Request {
  private String action_type_id;

  private String entity_type_id;

  private ActionScopeType action_scope;

  private String object_type_id;

  private Boolean owner_user_only = false;
}
