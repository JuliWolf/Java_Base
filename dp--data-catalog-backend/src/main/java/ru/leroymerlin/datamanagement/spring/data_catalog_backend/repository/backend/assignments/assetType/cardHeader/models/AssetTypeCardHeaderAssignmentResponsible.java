package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssetTypeCardHeaderAssignmentResponsible {
  private UUID responsibilityId;

  private ResponsibleType responsibleType;

  private UUID responsibleUserId;

  private String responsibleUserName;

  private String responsibleUserFirstName;

  private String responsibleUserLastName;

  private UUID responsibleGroupId;

  private String responsibleGroupName;

  public UUID getResponsibleId () {
    if (responsibleType == null) return null;

    if (responsibleType.equals(ResponsibleType.GROUP)) {
      return responsibleGroupId;
    }

    return responsibleUserId;
  }

  public String getResponsibleName () {
    if (responsibleType == null) return null;

    if (responsibleType.equals(ResponsibleType.GROUP)) {
      return responsibleGroupName;
    }

    return responsibleUserName;
  }

  public String getResponsibleFullName () {
    if (responsibleType == null) return null;

    if (responsibleType.equals(ResponsibleType.GROUP)) {
      return responsibleGroupName;
    }

    return responsibleUserFirstName + " " + responsibleUserLastName;
  }
}
