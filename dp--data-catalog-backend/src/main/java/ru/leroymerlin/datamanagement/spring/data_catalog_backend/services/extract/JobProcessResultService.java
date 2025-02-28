package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract;

import java.util.List;

/**
 * @author juliwolf
 */

public interface JobProcessResultService<T, Y> {
  void processSuccessResult(List<T> responseList, List<Y> requests);

  void processErrorResult(List<Y> requests);
}
