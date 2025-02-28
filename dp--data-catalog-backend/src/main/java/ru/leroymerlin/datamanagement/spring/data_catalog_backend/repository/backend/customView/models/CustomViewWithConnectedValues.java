package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.models;

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
public class CustomViewWithConnectedValues {
  private UUID customViewId;

  private UUID asseTypeId;

  private String assetTypeName;

  private String customViewName;

  private UUID roleId;

  private String roleName;

  private String headerRowNames;

  private String headerPrepareQuery;

  private String headerSelectQuery;

  private String headerClearQuery;

  private String tableColumnNames;

  private String tablePrepareQuery;

  private String tableSelectQuery;

  private String tableClearQuery;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  private java.sql.Timestamp lastModifiedOn;

  private UUID lastModifiedBy;
}
