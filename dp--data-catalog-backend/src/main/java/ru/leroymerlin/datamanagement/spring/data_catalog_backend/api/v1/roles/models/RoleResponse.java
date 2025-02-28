package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class RoleResponse implements Response {
  private UUID role_id;

  private String role_name;

  private String role_description;

  private Long usage_count;

  private Long responsibilities_usage_count;

  private Long global_responsibilities_usage_count;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;
}
