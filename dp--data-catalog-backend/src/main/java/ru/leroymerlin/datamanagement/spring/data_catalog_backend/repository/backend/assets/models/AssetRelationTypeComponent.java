package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssetRelationTypeComponent {
  private UUID relationTypeId;

  private UUID relationTypeComponentId;

  private String relationTypeComponentName;

  private Long total;
}
