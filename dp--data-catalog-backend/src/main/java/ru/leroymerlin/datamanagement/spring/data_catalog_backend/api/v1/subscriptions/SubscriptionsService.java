package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.InvalidCronExpressionException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.SubscriptionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.get.GetSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.get.GetSubscriptionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

/**
 * @author juliwolf
 */

public interface SubscriptionsService {
  PostSubscriptionResponse createSubscription (
    PostSubscriptionRequest subscriptionRequest,
    User user
  ) throws UserNotFoundException, AssetNotFoundException, InvalidCronExpressionException;

  PatchSubscriptionResponse updateSubscription (
    UUID subscriptionId,
    PatchSubscriptionRequest subscriptionRequest,
    User user
  ) throws SubscriptionNotFoundException, InvalidCronExpressionException;

  GetSubscriptionResponse getSubscriptionById (UUID subscriptionId) throws SubscriptionNotFoundException;

  GetSubscriptionsResponse getSubscriptionsByParams (
    UUID assetId,
    UUID userId,
    List<UUID> assetTypeIds,
    List<UUID> lifecycleStatusId,
    List<UUID> stewardshipStatusId,
    SortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  );

  void deleteSubscriptionById (UUID subscriptionId, User user) throws SubscriptionNotFoundException;
}
