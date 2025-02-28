package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post;

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
public class PostRelationRequest {
  private String asset_id;

  private String component_id;
}
