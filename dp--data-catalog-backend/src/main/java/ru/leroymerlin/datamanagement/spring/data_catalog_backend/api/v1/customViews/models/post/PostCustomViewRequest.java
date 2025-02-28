package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewTableColumnName;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostCustomViewRequest implements Request {
  private String asset_type_id;

  private String custom_view_name;

  private String role_id;

  private List<CustomViewHeaderRowName> header_row_names = new ArrayList<>();

  private String header_prepare_query;

  private String header_select_query;

  private String header_clear_query;

  private List<CustomViewTableColumnName> table_column_names = new ArrayList<>();

  private String table_prepare_query;

  private String table_select_query;

  private String table_clear_query;
}
