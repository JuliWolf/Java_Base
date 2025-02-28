package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions;

import java.util.UUID;

/**
 * @author JuliWolf
 */
public class SubscriptionNotFoundException extends RuntimeException {
  public SubscriptionNotFoundException () {
    super("subscription not found");
  }

  public SubscriptionNotFoundException (UUID subscriptionId) {
    super("Subscription with id "+ subscriptionId + " not found");
  }
}
