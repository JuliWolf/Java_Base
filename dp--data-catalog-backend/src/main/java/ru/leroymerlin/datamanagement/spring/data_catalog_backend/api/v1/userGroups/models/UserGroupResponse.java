package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserGroupResponse implements Response {
  private UUID user_group_id;

  private UUID user_id;

  private String username;

  private UUID group_id;

  private String groupname;

  private java.sql.Timestamp added_on;

  private UUID added_by;
}
