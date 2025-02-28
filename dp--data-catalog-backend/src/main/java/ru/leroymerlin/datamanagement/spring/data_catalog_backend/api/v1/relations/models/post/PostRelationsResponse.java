package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post;

import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@Getter
@Setter
public class PostRelationsResponse implements Response, PostRelationsWithComponents {
  private UUID relation_id;

  private UUID relation_type_id;

  private List<PostRelationResponse> component;

  private List<AbstractMap.SimpleEntry<UUID, UUID>> componentAssetEntrySet;

  public PostRelationsResponse (UUID relation_id, UUID relation_type_id, List<PostRelationResponse> component) {
    this.relation_id = relation_id;
    this.relation_type_id = relation_type_id;
    this.component = component;

    this.componentAssetEntrySet = mapComponentsToEntries();
  }

  @Override
  public UUID getRelationTypeId () {
    return this.relation_type_id;
  }

  private List<AbstractMap.SimpleEntry<UUID, UUID>> mapComponentsToEntries () {
    return this.component.stream()
      .map(component -> new AbstractMap.SimpleEntry<>(component.getRelation_type_component_id(), component.getAsset_id()))
      .toList();
  }

  @Override
  public boolean equals (Object object) {
    if (this == object) return true;

    if (object == null || !(object instanceof PostRelationsWithComponents)) return false;

    PostRelationsWithComponents that = (PostRelationsWithComponents) object;

    if (!Objects.equals(getRelationTypeId(), that.getRelationTypeId())) return false;

    if (getComponentAssetEntrySet().size() != that.getComponentAssetEntrySet().size()) return false;

    return new HashSet<>(getComponentAssetEntrySet()).containsAll(that.getComponentAssetEntrySet());
  }

  @Override
  public int hashCode () {
    return Objects.hash(getRelationTypeId(), getComponentAssetEntrySet());
  }
}
