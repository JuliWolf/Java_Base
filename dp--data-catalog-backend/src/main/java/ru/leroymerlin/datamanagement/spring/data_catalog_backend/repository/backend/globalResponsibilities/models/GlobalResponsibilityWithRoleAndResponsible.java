package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.models;

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
public class GlobalResponsibilityWithRoleAndResponsible {
  private UUID globalResponsibilityId;

  private UUID responsibleGroupId;

  private String responsibleGroupName;

  private UUID responsibleUserId;

  private String responsibleUserName;

  private ResponsibleType responsibleType;

  private UUID roleId;

  private String roleName;

  private String roleDescription;

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
}
