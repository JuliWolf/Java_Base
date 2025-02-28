package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupResponse implements Response {
  private UUID group_id;

  private String group_name;

  private String group_description;

  private String group_email;

  private String group_messenger;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;
}
