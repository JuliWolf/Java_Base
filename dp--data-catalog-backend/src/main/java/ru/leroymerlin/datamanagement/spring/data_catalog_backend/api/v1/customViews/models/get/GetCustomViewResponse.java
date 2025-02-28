package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.models.CustomViewWithConnectedValues;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetCustomViewResponse implements Response {
  private UUID custom_view_id;

  private UUID asset_type_id;

  private String asset_type_name;

  private String custom_view_name;

  private UUID role_id;

  private String role_name;

  private List header_row_names;

  private String header_prepare_query;

  private String header_select_query;

  private String header_clear_query;

  private List table_column_names;

  private String table_prepare_query;

  private String table_select_query;

  private String table_clear_query;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;

  public GetCustomViewResponse (CustomViewWithConnectedValues customView) throws JsonProcessingException {
    this.custom_view_id = customView.getCustomViewId();
    this.asset_type_id = customView.getAsseTypeId();
    this.asset_type_name = customView.getAssetTypeName();
    this.custom_view_name = customView.getCustomViewName();
    this.role_id = customView.getRoleId();
    this.role_name = customView.getRoleName();
    this.header_prepare_query = customView.getHeaderPrepareQuery();
    this.header_select_query = customView.getHeaderSelectQuery();
    this.header_clear_query = customView.getHeaderClearQuery();
    this.table_prepare_query = customView.getTablePrepareQuery();
    this.table_select_query = customView.getTableSelectQuery();
    this.table_clear_query = customView.getTableClearQuery();
    this.created_on = customView.getCreatedOn();
    this.created_by = customView.getCreatedBy();
    this.last_modified_on = customView.getLastModifiedOn();
    this.last_modified_by = customView.getLastModifiedBy();

    if (customView.getHeaderRowNames() == null && customView.getTableColumnNames() == null) return;

    ObjectMapper objectMapper = new ObjectMapper();

    if (customView.getHeaderRowNames() != null) {
      this.header_row_names = objectMapper.readValue(customView.getHeaderRowNames(), List.class);
    }

    if (customView.getTableColumnNames() != null) {
      this.table_column_names = objectMapper.readValue(customView.getTableColumnNames(), List.class);
    }
  }
}
