package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.models.SubscriptionWithConnectedValues;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetSubscriptionResponse implements Response {
  private UUID subscription_id;

  private UUID asset_id;

  private String asset_name;

  private String asset_displayname;

  private UUID asset_type_id;

  private String asset_type_name;

  private UUID stewardship_status_id;

  private String stewardship_status_name;

  private UUID lifecycle_status_id;

  private String lifecycle_status_name;

  private UUID user_id;

  private String username;

  private String first_name;

  private String last_name;

  private String notification_schedule;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;

  public GetSubscriptionResponse (SubscriptionWithConnectedValues subscription) {
    this.subscription_id = subscription.getSubscriptionId();
    this.asset_id = subscription.getAssetId();
    this.asset_name = subscription.getAssetName();
    this.asset_displayname = subscription.getAsset_DisplayName();
    this.asset_type_id = subscription.getAssetTypeId();
    this.asset_type_name = subscription.getAssetTypeName();
    this.stewardship_status_id = subscription.getStewardshipStatusId();
    this.stewardship_status_name = subscription.getStewardshipStatusName();
    this.lifecycle_status_id = subscription.getLifecycleStatusId();
    this.lifecycle_status_name = subscription.getLifecycleStatusName();
    this.user_id = subscription.getUserId();
    this.username =  subscription.getUsername();
    this.first_name = subscription.getFirstName();
    this.last_name =  subscription.getLastName();
    this.notification_schedule =  subscription.getNotificationSchedule();
    this.created_on =  subscription.getCreatedOn();
    this.created_by = subscription.getCreatedBy();
    this.last_modified_on = subscription.getLastModifiedOn();
    this.last_modified_by = subscription.getLastModifiedBy();
  }
}
