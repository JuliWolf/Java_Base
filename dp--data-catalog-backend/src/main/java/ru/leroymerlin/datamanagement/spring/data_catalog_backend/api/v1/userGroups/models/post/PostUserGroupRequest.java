package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author JuliWolf
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUserGroupRequest implements Request {
  private String user_id;

  private String group_id;
}
