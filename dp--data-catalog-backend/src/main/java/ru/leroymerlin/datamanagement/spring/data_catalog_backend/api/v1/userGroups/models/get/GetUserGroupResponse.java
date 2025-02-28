package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetUserGroupResponse {
  private UUID user_group_id;

  private UUID user_id;

  private String username;

  private String first_name;

  private String last_name;

  private UUID group_id;

  private String groupname;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
