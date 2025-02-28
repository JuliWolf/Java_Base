package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.models;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class ProfileResponse implements Response {
  private UUID user_id;

  private String username;

  private String email;

  private String first_name;

  private String last_name;

  private SourceType source;

  private String language;

  private java.sql.Timestamp created_date_time;

  private java.sql.Timestamp last_auth_time;
}
