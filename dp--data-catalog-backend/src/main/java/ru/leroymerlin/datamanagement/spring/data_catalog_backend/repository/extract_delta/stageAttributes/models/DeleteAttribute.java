package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models;

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
public class DeleteAttribute {
  private Long stageAttributeId;

  private UUID matchedAttributeId;
  public UUID getRequest () {
    return this.matchedAttributeId;
  }
}
