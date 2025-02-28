package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models;

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
public class DeleteRelation {
  private Long stageRelationId;

  private UUID matchedRelationId;
  public UUID getRequest () {
    return this.matchedRelationId;
  }
}
