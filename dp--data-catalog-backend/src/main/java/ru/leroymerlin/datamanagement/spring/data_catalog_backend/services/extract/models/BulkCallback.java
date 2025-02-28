package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models;

import java.util.List;

/**
 * @author juliwolf
 */

public interface BulkCallback<T, Y> {
  List<Y> loadData (Integer pageNumber, Integer pageSize);

  List<T> callBatchRequest (List<Y> requestData);

  void updateCount(RequestResult requestResult, Long count);

  void processSuccessResponse(List<T> responseList, List<Y> requests);

  void processErrorResponse(List<Y> response);
}
