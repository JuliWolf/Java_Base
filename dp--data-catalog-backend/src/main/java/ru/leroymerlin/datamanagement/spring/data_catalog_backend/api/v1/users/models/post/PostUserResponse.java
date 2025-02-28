package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PostUserResponse implements Response {
  private UUID user_id;

  private String username;

  private String email;

  private String first_name;

  private String last_name;

  private SourceType source;

  private String boss_k_pid;

  private UUID struct_unit_id;

  private UserType user_type;

  private UserWorkStatus user_work_status;

  private UserPhotoLink user_photo_link;

  private String language;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
