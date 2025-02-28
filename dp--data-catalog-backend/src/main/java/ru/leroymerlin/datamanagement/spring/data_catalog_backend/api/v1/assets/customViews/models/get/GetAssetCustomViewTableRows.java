package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.domain.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAssetCustomViewTableRows implements Response {
  long total;

  int page_size;

  int page_number;

  private List<GetAssetCustomViewTableRow> results;

  public GetAssetCustomViewTableRows (PageRequest pageRequest, List<Object[]> resultList) {
    this.total = resultList.size();
    this.page_size = pageRequest.getPageSize();
    this.page_number = pageRequest.getPageNumber();

    AtomicLong indexCounter = new AtomicLong(0);

    List<GetAssetCustomViewTableRow> list = resultList.stream()
      .skip(pageRequest.getOffset())
      .limit(pageRequest.getPageSize())
      .map(item -> new GetAssetCustomViewTableRow(
        indexCounter.incrementAndGet(),
        convertArrayOfObjectsIntoList(item)
      ))
      .toList();

    this.results = list;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  static class GetAssetCustomViewTableRow {
    private Long row_number;

    private List<String> role_values;
  }

  private List<String> convertArrayOfObjectsIntoList (Object[] values) {
    List resultList = new ArrayList<>();

    for (int i = 0; i < values.length; i ++) {
      Object value = values[i];
      resultList.add(value != null ? value.toString() : value);
    }

    return resultList;
  }
}
