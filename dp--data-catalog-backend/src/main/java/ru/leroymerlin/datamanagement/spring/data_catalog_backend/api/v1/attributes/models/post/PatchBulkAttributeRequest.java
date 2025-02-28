package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.BulkItemRequest;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatchBulkAttributeRequest implements Request, BulkItemRequest {
  private UUID attribute_id;

  private String value;

  @Override
  @JsonIgnore
  public UUID getRequestItemId () {
    return attribute_id;
  }
}
