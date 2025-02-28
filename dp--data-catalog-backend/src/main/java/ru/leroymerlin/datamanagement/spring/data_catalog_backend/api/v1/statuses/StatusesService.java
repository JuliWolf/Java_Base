package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.StatusResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.get.GetStatusesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PatchStatusRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusResponse;

public interface StatusesService {
  PostStatusResponse createStatus (PostStatusRequest statusRequest, User user);

  StatusResponse updateStatus (UUID statusId, PatchStatusRequest statusRequest, User user) throws StatusNotFoundException;

  StatusResponse getStatusById (UUID statusId) throws StatusNotFoundException;

  GetStatusesResponse getStatusesByParams (String statusName, String statusDescription, Integer pageNumber, Integer pageSize);

  void deleteStatusById (UUID statusId, User user) throws StatusNotFoundException, StatusIsUsedForAssetException;
}
