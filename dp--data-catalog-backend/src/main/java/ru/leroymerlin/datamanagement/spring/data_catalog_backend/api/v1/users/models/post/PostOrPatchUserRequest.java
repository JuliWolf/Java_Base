package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PostOrPatchUserRequest implements Request {
  private String username;

  private String first_name;

  private String last_name;

  private String email;

  private String boss_k_pid;

  private String struct_unit_id;

  private UserWorkStatus user_work_status;

  private String user_photo_link;
}
