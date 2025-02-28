package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SubscriptionWithConnectedValues {
  private UUID subscriptionId;

  private UUID assetId;

  private String assetName;

  private String asset_DisplayName;

  private UUID assetTypeId;

  private String assetTypeName;

  private UUID stewardshipStatusId;

  private String stewardshipStatusName;

  private UUID lifecycleStatusId;

  private String lifecycleStatusName;

  private UUID userId;

  private String username;

  private String firstName;

  private String lastName;

  private String notificationSchedule;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  private java.sql.Timestamp lastModifiedOn;

  private UUID lastModifiedBy;
}
