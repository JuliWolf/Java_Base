package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models;

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
public class ResponsibilityToDelete {
  private UUID responsibilityId;

  private UUID assetId;

  private Boolean inheritedFlag;

  private UUID relationId;
}
