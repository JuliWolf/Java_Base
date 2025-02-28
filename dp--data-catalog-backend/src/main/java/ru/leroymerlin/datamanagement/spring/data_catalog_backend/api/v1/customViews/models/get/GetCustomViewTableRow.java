package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get;

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
public class GetCustomViewTableRow {
  private Integer row_number;

  private String[] row_values;
}
