package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeRequest;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PostAttribute {
  private Long stageAttributeId;

  private UUID attributeTypeId;

  private UUID assetId;

  private String value;

  public PostAttributeRequest getRequest () {
    return new PostAttributeRequest(
      UUIDUtils.convertUUIDToString(this.attributeTypeId),
      this.assetId,
      this.value
    );
  }
}
