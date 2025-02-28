package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetRelationType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetRelationTypeComponent;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@Setter
@Getter
public class GetAssetRelationType {
  private UUID relation_type_id;

  private String relation_type_name;

  private Long count;

  private List<GetAssetRelationTypeComponent> relation_type_component;

  public void setRelation_type_component ( List<AssetRelationTypeComponent> relationTypeComponents) {
    this.relation_type_component = relationTypeComponents.stream()
      .map(relationTypeComponent -> new GetAssetRelationTypeComponent(
        relationTypeComponent.getRelationTypeComponentId(),
        relationTypeComponent.getRelationTypeComponentName(),
        relationTypeComponent.getTotal()
      ))
      .toList();
  }

  public GetAssetRelationType (AssetRelationType relationType, List<AssetRelationTypeComponent> relationTypeComponents) {
    this.relation_type_id = relationType.getRelationTypeId();
    this.relation_type_name = relationType.getRelationTypeName();
    this.count = relationType.getTotal();

    setRelation_type_component(relationTypeComponents);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Setter
  @Getter
  private static class GetAssetRelationTypeComponent {
    private UUID relation_type_component_id;

    private String relation_type_component_name;

    private Long count;
  }
}
