package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class StageCountByDecision {
  private ActionDecision actionDecision;

  private Long count;
}
