package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAssetCustomViewHeaderRows implements Response {
  private UUID asset_id;

  private List<GetAssetCustomViewHeaderRow> header_rows;

  public GetAssetCustomViewHeaderRows (UUID assetId, List<Object[]> resultList, List<CustomViewHeaderRowName> headerRows) {
    this.asset_id = assetId;

    if (resultList.isEmpty()) return;

    boolean isArray = resultList.get(0) instanceof Object[];
    AtomicInteger indexCounter = new AtomicInteger(0);

    this.header_rows = headerRows.stream()
      .map(item -> {
        int index = indexCounter.getAndIncrement();
        Object value = isArray ? resultList.get(0)[index] : resultList.get(0);

        return new GetAssetCustomViewHeaderRow(
          item.getRow_name(),
          item.getRow_kind(),
          value != null ? value.toString() : null
        );
      }).toList();
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  static class GetAssetCustomViewHeaderRow {
    private String row_name;

    private AttributeKindType row_kind;

    private String row_value;
  }
}
