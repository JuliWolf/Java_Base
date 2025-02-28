package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GetUserResponse implements Response {
  private UUID user_id;

  private String username;

  private String email;

  private String first_name;

  private String last_name;

  private SourceType source;

  private String language;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;

  private java.sql.Timestamp last_auth_time;
}
