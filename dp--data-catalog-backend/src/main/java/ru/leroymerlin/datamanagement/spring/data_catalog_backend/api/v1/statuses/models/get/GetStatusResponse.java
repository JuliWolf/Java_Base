package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GetStatusResponse implements Response {
  private UUID status_id;

  private String status_name;

  private String status_description;

  private Long status_usage_count;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;
}
