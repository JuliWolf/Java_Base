package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews;

import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

public interface CustomViewsService {
  PostCustomViewResponse createCustomView (
    PostCustomViewRequest customViewRequest,
    User user
  ) throws
    RoleNotFoundException,
    JsonProcessingException,
    AssetTypeNotFoundException;

  PatchCustomViewResponse updateCustomView (
    UUID customViewId,
    PatchCustomViewRequest customViewRequest,
    User user
  ) throws
    RoleNotFoundException,
    JsonProcessingException,
    CustomViewNotFoundException;

  GetCustomViewsResponse getCustomViewsByParams (UUID roleId, UUID assetTypeId, String customViewName, Integer pageNumber, Integer pageSize);

  GetCustomViewResponse getCustomViewById (UUID customViewId) throws CustomViewNotFoundException;

  void deleteCustomViewById (UUID customViewId, User user) throws CustomViewNotFoundException;
}
