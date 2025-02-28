package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.interfaces.AttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.BulkItemRequest;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostAttributeRequest implements Request, AttributeTypeRequest, BulkItemRequest {
  private String attribute_type_id;

  private UUID asset_id;

  private String value;

  @Override
  @JsonIgnore
  public UUID getRequestItemId () {
    return asset_id;
  }
}
