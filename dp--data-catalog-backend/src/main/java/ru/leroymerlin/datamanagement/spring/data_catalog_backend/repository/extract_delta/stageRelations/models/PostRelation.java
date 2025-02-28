package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models;

import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsWithComponents;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@Getter
@Setter
public class PostRelation implements PostRelationsWithComponents {
  private Long stageRelationId;

  private UUID relationTypeId;

  private List<PostRelationComponent> relationComponents;

  private List<AbstractMap.SimpleEntry<UUID, UUID>> componentAssetEntrySet;

  public PostRelation (
    Long stageRelationId,
    UUID relationTypeId,
    UUID asset1Id,
    UUID relationTypeComponent1Id,
    UUID asset2Id,
    UUID relationTypeComponent2Id
  ) {
    this.stageRelationId = stageRelationId;
    this.relationTypeId = relationTypeId;

    this.relationComponents = List.of(
      new PostRelationComponent(asset1Id, relationTypeComponent1Id),
      new PostRelationComponent(asset2Id, relationTypeComponent2Id)
    );

    this.componentAssetEntrySet = mapComponentsToEntries();
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class PostRelationComponent {
    private UUID assetId;

    private UUID relationComponentId;
  }

  private List<AbstractMap.SimpleEntry<UUID, UUID>> mapComponentsToEntries () {
    return this.relationComponents.stream()
      .map(component -> new AbstractMap.SimpleEntry<>(component.getRelationComponentId(), component.getAssetId()))
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

  public PostRelationsRequest getRequest () {
    return new PostRelationsRequest(
      UUIDUtils.convertUUIDToString(this.relationTypeId),
      this.relationComponents.stream()
        .map(component -> new PostRelationRequest(
          UUIDUtils.convertUUIDToString(component.getAssetId()),
          UUIDUtils.convertUUIDToString(component.getRelationComponentId())
        ))
        .toList()
    );
  }
}
