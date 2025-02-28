package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models;

import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
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
public class RelationTypeWithSortedRelationTypeComponentList {
  private UUID relationTypeId;

  private List<AbstractMap.SimpleEntry<UUID, UUID>> relationTypeComponents;

  @Override
  public boolean equals (Object object) {
    if (this == object) return true;

    if (object == null || getClass() != object.getClass()) return false;

    RelationTypeWithSortedRelationTypeComponentList that = (RelationTypeWithSortedRelationTypeComponentList) object;

    return Objects.equals(getRelationTypeId(), that.getRelationTypeId()) && Objects.equals(getRelationTypeComponents().toString(), that.getRelationTypeComponents().toString());
  }

  @Override
  public int hashCode () {
    return Objects.hash(getRelationTypeId(), getRelationTypeComponents());
  }
}
