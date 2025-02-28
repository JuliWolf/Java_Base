package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Status;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Subscription;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.SubscriptionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.InvalidCronExpressionException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.SubscriptionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.get.GetSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class SubscriptionsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private SubscriptionRepository subscriptionRepository;

  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private StatusRepository statusRepository;

  @Autowired
  private SubscriptionsService subscriptionsService;

  private Asset asset;

  @BeforeEach
  public void prepareData () {
    Status status = statusRepository.save(new Status("some name", "another name", language, user));
    AssetType assetType = assetTypeRepository.save(new AssetType("asset type name", "description", "atn", "red", language, user));

    asset = assetRepository.save(new Asset("asset name", assetType, "an", language, null, status, user));
  }

  @AfterEach
  public void clearTables () {
    subscriptionRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    statusRepository.deleteAll();
  }

  @Test
  public void createSubscriptionUserNotFoundIntegrationTest () {
    PostSubscriptionRequest postRequest = new PostSubscriptionRequest(UUID.randomUUID().toString(), asset.getAssetId().toString(), "* */5 * * * *");

    assertThrows(UserNotFoundException.class, () -> subscriptionsService.createSubscription(postRequest, user));
  }

  @Test
  public void createSubscriptionAssetNotFoundIntegrationTest () {
    PostSubscriptionRequest postRequest = new PostSubscriptionRequest(user.getUserId().toString(), UUID.randomUUID().toString(), "* */5 * * * *");

    assertThrows(AssetNotFoundException.class, () -> subscriptionsService.createSubscription(postRequest, user));
  }

  @Test
  public void createSubscriptionInvalidCronExpressionIntegrationTest () {
    PostSubscriptionRequest postRequest = new PostSubscriptionRequest(user.getUserId().toString(), asset.getAssetId().toString(), "* */5 * * *");

    assertThrows(InvalidCronExpressionException.class, () -> subscriptionsService.createSubscription(postRequest, user));
  }

  @Test
  public void createSubscriptionSubscriptionAlreadyExistsIntegrationTest () {
    subscriptionRepository.save(new Subscription(user, asset, "* */5 * * * *", user));
    PostSubscriptionRequest postRequest = new PostSubscriptionRequest(user.getUserId().toString(), asset.getAssetId().toString(), "* */5 * * * *");

    assertThrows(DataIntegrityViolationException.class, () -> subscriptionsService.createSubscription(postRequest, user));
  }

  @Test
  public void createSubscriptionSuccessIntegrationTest () {
    PostSubscriptionRequest postRequest = new PostSubscriptionRequest(user.getUserId().toString(), asset.getAssetId().toString(), "* */5 * * * *");

    PostSubscriptionResponse subscription = subscriptionsService.createSubscription(postRequest, user);

    assertAll(
      () -> assertEquals(user.getUserId(), subscription.getUser_id()),
      () -> assertEquals(asset.getAssetId(), subscription.getAsset_id()),
      () -> assertEquals("* */5 * * * *", subscription.getNotification_schedule())
    );
  }

  @Test
  public void updateSubscriptionSubscriptionNotFoundIntegrationTest () {
    PatchSubscriptionRequest patchRequest = new PatchSubscriptionRequest("* */5 * * * *");

    assertThrows(SubscriptionNotFoundException.class, () -> subscriptionsService.updateSubscription(UUID.randomUUID(), patchRequest, user));
  }

  @Test
  public void updateSubscriptionInvalidCronExpressionIntegrationTest () {
    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* */5 * * * *", user));
    PatchSubscriptionRequest patchRequest = new PatchSubscriptionRequest("* */5 * * *");

    assertThrows(InvalidCronExpressionException.class, () -> subscriptionsService.updateSubscription(subscription.getSubscriptionId(), patchRequest, user));
  }

  @Test
  public void updateSubscriptionSuccessIntegrationTest () {
    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* * * * * *", user));
    PatchSubscriptionRequest patchRequest = new PatchSubscriptionRequest("* */5 * * * *");

    PatchSubscriptionResponse response = subscriptionsService.updateSubscription(subscription.getSubscriptionId(), patchRequest, user);

    assertAll(
      () -> assertNotNull(response.getLast_modified_on()),
      () -> assertNotNull(response.getLast_modified_by()),
      () -> assertEquals("* */5 * * * *", response.getNotification_schedule())
    );
  }

  @Test
  public void getSubscriptionByIdSubscriptionNotFoundIntegrationTest () {
    assertThrows(SubscriptionNotFoundException.class, () -> subscriptionsService.getSubscriptionById(UUID.randomUUID()));
  }

  @Test
  public void getSubscriptionByIdSuccessIntegrationTest () {
    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* * * * * *", user));
    GetSubscriptionResponse response = subscriptionsService.getSubscriptionById(subscription.getSubscriptionId());

    assertAll(
      () -> assertEquals(user.getUserId(), response.getUser_id()),
      () -> assertEquals(asset.getAssetId(), response.getAsset_id()),
      () -> assertEquals("* * * * * *", response.getNotification_schedule()),
      () -> assertNotNull(response.getStewardship_status_id())
    );
  }

  @Test
  public void getSubscriptionsByParamsIntegrationTest () {
    Status secondStewardshipStatus = statusRepository.save(new Status("second stewardship status", "another name", language, user));
    AssetType secondAssetType = assetTypeRepository.save(new AssetType("second asset type name", "description", "atn", "red", language, user));
    Asset secondAsset = assetRepository.save(new Asset("second asset name", secondAssetType, "an", language, null, secondStewardshipStatus, user));

    Status lifecicleStatus = statusRepository.save(new Status("third lifecycle status", "another name", language, user));
    AssetType thirdAssetType = assetTypeRepository.save(new AssetType("third asset type name", "description", "atn", "red", language, user));
    Asset thirdAsset = assetRepository.save(new Asset("third asset name", thirdAssetType, "an", language, lifecicleStatus, null, user));

    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* * * * * *", user));
    Subscription secondSubscription = subscriptionRepository.save(new Subscription(user, secondAsset, "* * * * * *", user));
    Subscription thirdSubscription = subscriptionRepository.save(new Subscription(user, thirdAsset, "* * * * * *", user));

    assertAll(
      () -> assertEquals(3, subscriptionsService.getSubscriptionsByParams(null, null, null, null,null, null,null, 0, 50).getResults().size()),
      () -> assertEquals(1, subscriptionsService.getSubscriptionsByParams(asset.getAssetId(), null, null, null,null, null,null, 0, 50).getResults().size()),
      () -> assertEquals(3, subscriptionsService.getSubscriptionsByParams(null, user.getUserId(), null, null,null, null,null, 0, 50).getResults().size()),
      () -> assertEquals(1, subscriptionsService.getSubscriptionsByParams(null, null, null, List.of(lifecicleStatus.getStatusId()),null, null,null, 0, 50).getResults().size()),
      () -> assertEquals(0, subscriptionsService.getSubscriptionsByParams(null, null, null, List.of(lifecicleStatus.getStatusId()), List.of(secondStewardshipStatus.getStatusId()), null,null, 0, 50).getResults().size()),
      () -> assertEquals(1, subscriptionsService.getSubscriptionsByParams(null, null, null, null, List.of(secondStewardshipStatus.getStatusId()), null,null, 0, 50).getResults().size()),
      () -> assertEquals(1, subscriptionsService.getSubscriptionsByParams(null, null, List.of(thirdAssetType.getAssetTypeId(), secondAssetType.getAssetTypeId()), null, List.of(secondStewardshipStatus.getStatusId()), null,null, 0, 50).getResults().size()),
      () -> assertEquals(2, subscriptionsService.getSubscriptionsByParams(null, null, List.of(thirdAssetType.getAssetTypeId(), secondAssetType.getAssetTypeId()), null, null, null,null, 0, 50).getResults().size())
    );
  }

  @Test
  public void getSubscriptionsByParamsPaginationIntegrationTest () {
    generateSubscriptions(130);

    assertAll(
      () -> assertEquals(100, subscriptionsService.getSubscriptionsByParams(null, null, null, null, null, null,null, 0, 150).getResults().size()),
      () -> assertEquals(0, subscriptionsService.getSubscriptionsByParams(null, null, null, null, null, null,null, 10, 50).getResults().size()),
      () -> assertEquals(130, subscriptionsService.getSubscriptionsByParams(null, null, null, null, null, null,null, 0, 50).getTotal())
    );
  }

  @Test
  public void deleteSubscriptionByIdSubscriptionNotFoundIntegrationTest () {
    assertThrows(SubscriptionNotFoundException.class, () -> subscriptionsService.deleteSubscriptionById(UUID.randomUUID(), user));
  }

  @Test
  public void deleteSubscriptionByIdAlreadyDeletedIntegrationTest () {
    Subscription subscription = new Subscription(user, asset, "* * * * * *", user);
    subscription.setIsDeleted(true);
    subscriptionRepository.save(subscription);

    assertThrows(SubscriptionNotFoundException.class, () -> subscriptionsService.deleteSubscriptionById(UUID.randomUUID(), user));
  }

  @Test
  public void deleteSubscriptionByIdSuccessIntegrationTest () {
    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* * * * * *", user));

    subscriptionsService.deleteSubscriptionById(subscription.getSubscriptionId(), user);

    Optional<Subscription> deletedSubscription = subscriptionRepository.findById(subscription.getSubscriptionId());

    assertAll(
      () -> assertTrue(deletedSubscription.get().getIsDeleted()),
      () -> assertNotNull(deletedSubscription.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedSubscription.get().getDeletedBy().getUserId())
    );
  }

  private void generateSubscriptions (int count) {
    for (int i = 0; i < count; i++) {
      AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_name_" + i, "description", "atn", "red", language, user));
      Asset asset = assetRepository.save(new Asset("asset_name_" + i, assetType, "an", language, null, null, user));

      subscriptionRepository.save(new Subscription(user, asset, "* * * * * *", user));
    }
  }
}
