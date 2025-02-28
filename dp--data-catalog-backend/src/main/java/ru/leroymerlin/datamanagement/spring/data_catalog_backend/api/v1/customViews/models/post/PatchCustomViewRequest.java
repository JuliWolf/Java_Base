package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post;

import java.util.List;
import java.util.Optional;
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
public class PatchCustomViewRequest implements Request {

  private String custom_view_name;

  private Optional<String> role_id;

  private Optional<List<CustomViewHeaderRowName>> header_row_names;

  private Optional<String> header_prepare_query;

  private Optional<String> header_select_query;

  private Optional<String> header_clear_query;

  private Optional<List<CustomViewTableColumnName>> table_column_names;

  private Optional<String> table_prepare_query;

  private Optional<String> table_select_query;

  private Optional<String> table_clear_query;
}
