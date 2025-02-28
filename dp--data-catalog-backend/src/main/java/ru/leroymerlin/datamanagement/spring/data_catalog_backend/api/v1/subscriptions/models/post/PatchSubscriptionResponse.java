package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatchSubscriptionResponse implements Response {
  private UUID subscription_id;

  private UUID user_id;

  private UUID asset_id;

  private String notification_schedule;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;
}
