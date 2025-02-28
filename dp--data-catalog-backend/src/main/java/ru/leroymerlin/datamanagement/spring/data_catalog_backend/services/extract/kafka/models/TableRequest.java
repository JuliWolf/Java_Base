package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Query;
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
public class TableRequest {
  private Query createTableQuery;

  private Query checkTableQuery;

  private List<Query> rows = new ArrayList<>();
}
