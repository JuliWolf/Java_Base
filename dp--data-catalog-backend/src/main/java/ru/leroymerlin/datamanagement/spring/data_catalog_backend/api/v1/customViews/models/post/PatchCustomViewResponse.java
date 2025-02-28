package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatchCustomViewResponse implements Response {
  private UUID custom_view_id;

  private UUID asset_type_id;

  private String custom_view_name;

  private UUID role_id;

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
}
