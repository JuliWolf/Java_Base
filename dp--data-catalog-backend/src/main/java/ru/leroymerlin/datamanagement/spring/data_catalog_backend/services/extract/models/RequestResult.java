package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models;

import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@Getter
@Setter
public class RequestResult {
  private AtomicLong errorCount;

  private AtomicLong postCount;

  private AtomicLong updateCount;

  private AtomicLong deleteCount;

  public RequestResult() {
    errorCount = new AtomicLong(0L);
    postCount = new AtomicLong(0L);
    updateCount = new AtomicLong(0L);
    deleteCount = new AtomicLong(0L);
  }

  public void updateErrorCount (Long count) {
    errorCount.updateAndGet(v -> v + count);
  }

  public void updatePostCount (Long count) {
    postCount.updateAndGet(v -> v + count);
  }

  public void updateDeleteCount (Long count) {
    deleteCount.updateAndGet(v -> v + count);
  }

  public void updateUpdateCount (Long count) {
    updateCount.updateAndGet(v -> v + count);
  }
}
