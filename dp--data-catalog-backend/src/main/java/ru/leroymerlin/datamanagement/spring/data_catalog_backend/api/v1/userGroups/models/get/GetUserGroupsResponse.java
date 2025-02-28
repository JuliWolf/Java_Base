package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.get;

import java.util.List;
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
public class GetUserGroupsResponse implements Response {
  private long total;

  private int page_size;

  private int page_number;

  private List<GetUserGroupResponse> results;
}
