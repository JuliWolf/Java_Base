package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchBulkAttributeRequest;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatchAttribute {
  private Long stageAttributeId;

  private UUID matchedAttributeId;

  private String value;

  public PatchBulkAttributeRequest getRequest () {
    return new PatchBulkAttributeRequest(
      this.matchedAttributeId,
      this.value
    );
  }
}
