package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Subscription;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.SubscriptionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.SubscriptionNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class SubscriptionsDAO {
  @Autowired
  protected SubscriptionRepository subscriptionRepository;

  public Subscription findSubscriptionById (UUID subscriptionId) {
    Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionId);

    if (subscription.isEmpty()) {
      throw new SubscriptionNotFoundException();
    }

    if (subscription.get().getIsDeleted()) {
      throw new SubscriptionNotFoundException();
    }

    return subscription.get();
  }

  public void deleteAllByAssetIds (List<UUID> assetIds, User user) {
    subscriptionRepository.deleteAllByAssetIds(assetIds, user.getUserId());
  }

  public void deleteAllByOwnerUserId (UUID ownerUserId, User user) {
    subscriptionRepository.deleteAllByOwnerUserId(ownerUserId, user.getUserId());
  }
}
