package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetChangeHistoryParams {
  private List<UUID> userIds;

  private List<AssetHistoryActionType> actionTypes;

  private List<AssetHistoryEntityType> entityTypes;

  private Date minDate;

  private Date maxDate;

  private Integer pageNumber;

  private Integer pageSize;
}
