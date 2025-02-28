package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models;

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
public class DeleteResponsibility {
  private Long stageResponsibilityId;

  private UUID matchedResponsibilityId;
  public UUID getRequest () {
    return this.matchedResponsibilityId;
  }
}
