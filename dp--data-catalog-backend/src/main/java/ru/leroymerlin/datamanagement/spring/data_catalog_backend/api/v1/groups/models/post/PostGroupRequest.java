package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post;

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
public class PostGroupRequest implements Request {
  private String group_name;

  private String group_description;

  private String group_email;

  private String group_messenger;
}
