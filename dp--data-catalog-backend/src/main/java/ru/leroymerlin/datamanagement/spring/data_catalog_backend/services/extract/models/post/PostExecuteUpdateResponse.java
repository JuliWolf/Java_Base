package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post;

import java.util.List;
import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PostExecuteUpdateResponse implements Response {
  private UUID job_id;

  private TotalItems total_items;

  private UpdatedItems updated;

  private InsertedItems inserted;

  private DeletedItems deleted;

  private ItemsErrors error;

  public boolean hasErrors () {
    return
      error.assets > 0 ||
      error.attributes > 0 ||
      error.relations > 0 ||
      error.responsibilities > 0;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @ToString
  public static class TotalItems {
    private Long assets;

    private Long attributes;

    private Long relations;

    private Long responsibilities;

    public TotalItems (
      List<StageCountByDecision> assetsCountList,
      List<StageCountByDecision> attributesCountList,
      List<StageCountByDecision> relationsCountList,
      List<StageCountByDecision> responsibilitiesCountList
    ) {
      this.assets = assetsCountList.stream().mapToLong(StageCountByDecision::getCount).sum();
      this.attributes = attributesCountList.stream().mapToLong(StageCountByDecision::getCount).sum();
      this.relations = relationsCountList.stream().mapToLong(StageCountByDecision::getCount).sum();
      this.responsibilities = responsibilitiesCountList.stream().mapToLong(StageCountByDecision::getCount).sum();
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @ToString
  public static class UpdatedItems {
    private Long assets;

    private Long attributes;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @ToString
  public static class InsertedItems {
    private Long assets;

    private Long attributes;

    private Long relations;

    private Long responsibilities;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @ToString
  public static class DeletedItems {
    private Long assets;

    private Long attributes;

    private Long relations;

    private Long responsibilities;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @ToString
  public static class ItemsErrors {
    private Long assets;

    private Long attributes;

    private Long relations;

    private Long responsibilities;
  }
}
