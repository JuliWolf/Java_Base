package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.models;

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
public class ResponsibilityWithConnectedValues {
  private UUID responsibilityId;

  private UUID responsibleGroupId;

  private String responsibleGroupName;

  private UUID responsibleUserId;

  private String responsibleUserName;

  private String responsibleUserFullName;

  private ResponsibleType responsibleType;

  private UUID roleId;

  private String roleName;

  private UUID assetId;

  private String assetDisplayName;

  private String assetName;

  private UUID assetTypeId;

  private String assetTypeName;

  private UUID stewardshipStatusId;

  private String stewardshipStatusName;

  private UUID lifecycleStatusId;

  private String lifecycleStatusName;

  private Boolean inheritedFlag;

  private UUID parentResponsibilityId;

  private UUID relationId;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  public UUID getResponsibleId () {
    if (responsibleType.equals(ResponsibleType.USER)) {
      return responsibleUserId;
    }

    return responsibleGroupId;
  }

  public String getResponsibleName () {
    if (responsibleType.equals(ResponsibleType.USER)) {
      return responsibleUserName;
    }

    return responsibleGroupName;
  }

  public String getResponsibleFullName () {
    if (responsibleType.equals(ResponsibleType.USER)) {
      return responsibleUserFullName;
    }

    return null;
  }
}
