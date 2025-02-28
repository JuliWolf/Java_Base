package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.BulkItemRequest;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "requestItemId")
public class PostResponsibilityRequest implements Request, BulkItemRequest {
  private UUID asset_id;

  private UUID role_id;

  private String responsible_type;

  private UUID responsible_id;

  @Override
  public UUID getRequestItemId () {
    return role_id;
  }
}
