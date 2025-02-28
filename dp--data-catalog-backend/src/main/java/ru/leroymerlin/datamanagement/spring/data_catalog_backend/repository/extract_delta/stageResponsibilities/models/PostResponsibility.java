package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PostResponsibility {
  private Long stageResponsibilityId;

  private UUID assetId;

  private UUID roleId;

  private UUID userId;

  private UUID groupId;

  private ResponsibleType responsibleType;

  public PostResponsibilityRequest getRequest () {
    return new PostResponsibilityRequest(
      this.assetId,
      this.roleId,
      this.responsibleType.toString(),
      this.responsibleType.equals(ResponsibleType.USER) ? this.userId : this.groupId
    );
  }
}
