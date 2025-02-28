package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostSubscriptionRequest implements Request {
  private String user_id;

  private String asset_id;

  private String notification_schedule;
}
