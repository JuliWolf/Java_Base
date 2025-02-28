package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PostStatusRequest implements Request {
  private String status_name;

  private String status_description;
}
