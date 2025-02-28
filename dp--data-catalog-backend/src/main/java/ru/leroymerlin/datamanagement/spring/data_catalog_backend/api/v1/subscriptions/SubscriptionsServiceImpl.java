package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Subscription;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.models.SubscriptionWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.SortUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.UsersDAO;

/**
 * @author juliwolf
 */

@Service
public class SubscriptionsServiceImpl extends SubscriptionsDAO implements SubscriptionsService {
  @Autowired
  private AssetsDAO assetsDAO;

  @Autowired
  private UsersDAO usersDAO;

  @Override
  public PostSubscriptionResponse createSubscription (
    PostSubscriptionRequest subscriptionRequest,
    User user
  ) throws UserNotFoundException, AssetNotFoundException, InvalidCronExpressionException {
    User userSubscription = usersDAO.findUserById(UUID.fromString(subscriptionRequest.getUser_id()));
    Asset asset = assetsDAO.findAssetById(UUID.fromString(subscriptionRequest.getAsset_id()));

    boolean isValid = CronExpression.isValidExpression(subscriptionRequest.getNotification_schedule());
    if (!isValid) {
      throw new InvalidCronExpressionException();
    }

    Subscription subscription = subscriptionRepository.save(new Subscription(
      userSubscription,
      asset,
      subscriptionRequest.getNotification_schedule(),
      user
    ));

    return new PostSubscriptionResponse(
      subscription.getSubscriptionId(),
      subscription.getOwnerUser().getUserId(),
      subscription.getAsset().getAssetId(),
      subscription.getNotificationSchedule(),
      subscription.getCreatedOn(),
      subscription.getCreatedByUUID()
    );
  }

  @Override
  public PatchSubscriptionResponse updateSubscription (
    UUID subscriptionId,
    PatchSubscriptionRequest subscriptionRequest,
    User user
  ) throws SubscriptionNotFoundException, InvalidCronExpressionException {
    Subscription subscription = findSubscriptionById(subscriptionId);

    boolean isValid = CronExpression.isValidExpression(subscriptionRequest.getNotification_schedule());
    if (!isValid) {
      throw new InvalidCronExpressionException();
    }

    subscription.setNotificationSchedule(subscriptionRequest.getNotification_schedule());
    subscription.setModifiedBy(user);
    subscription.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));

    Subscription updatedSubscription = subscriptionRepository.save(subscription);

    return new PatchSubscriptionResponse(
      updatedSubscription.getSubscriptionId(),
      updatedSubscription.getOwnerUser().getUserId(),
      updatedSubscription.getAsset().getAssetId(),
      updatedSubscription.getNotificationSchedule(),
      updatedSubscription.getCreatedOn(),
      updatedSubscription.getCreatedByUUID(),
      updatedSubscription.getLastModifiedOn(),
      updatedSubscription.getModifiedBy().getUserId()
    );
  }

  @Override
  public GetSubscriptionResponse getSubscriptionById (UUID subscriptionId) throws SubscriptionNotFoundException {
    Optional<SubscriptionWithConnectedValues> optional = subscriptionRepository.getSubscriptionWithConnectedValuesById(subscriptionId);

    if (optional.isEmpty()) {
      throw new SubscriptionNotFoundException();
    }

    SubscriptionWithConnectedValues subscription = optional.get();

    return new GetSubscriptionResponse(subscription);
  }

  @Override
  public GetSubscriptionsResponse getSubscriptionsByParams (
    UUID assetId,
    UUID userId,
    List<UUID> assetTypeIds,
    List<UUID> lifecycleStatusId,
    List<UUID> stewardshipStatusId,
    SortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<SubscriptionWithConnectedValues> responses = subscriptionRepository.findAllSubscriptionWithConnectedValuesByParamsPageable(
      assetId, userId,
      assetTypeIds != null ? assetTypeIds.size() : 0,
      assetTypeIds,
      lifecycleStatusId != null ? lifecycleStatusId.size() : 0,
      lifecycleStatusId,
      stewardshipStatusId != null ? stewardshipStatusId.size() : 0,
      stewardshipStatusId,
      PageRequest.of(pageNumber, pageSize, SortUtils.getSort(sortOrder, sortField != null ? sortField.getValue() : null, "u.username"))
    );

    List<GetSubscriptionResponse> subscriptions = responses.stream().map(GetSubscriptionResponse::new).toList();

    return new GetSubscriptionsResponse(
      responses.getTotalElements(),
      pageSize,
      pageNumber,
      subscriptions
    );
  }

  @Override
  public void deleteSubscriptionById (UUID subscriptionId, User user) throws SubscriptionNotFoundException {
    Subscription foundSubscription = findSubscriptionById(subscriptionId);

    foundSubscription.setIsDeleted(true);
    foundSubscription.setDeletedBy(user);
    foundSubscription.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    subscriptionRepository.save(foundSubscription);
  }
}
