package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetResponsibilityResponse implements Response {
  private UUID responsibility_id;

  private UUID responsible_id;

  private String responsible_name;

  private String responsible_fullname;

  private ResponsibleType responsible_type;

  private UUID role_id;

  private String role_name;

  private UUID asset_id;

  private String asset_name;

  private String asset_displayname;

  private UUID asset_type_id;

  private String asset_type_name;

  private UUID stewardship_status_id;

  private String stewardship_status_name;

  private UUID lifecycle_status_id;

  private String lifecycle_status_name;

  private Boolean inherited_flag;

  private UUID parent_responsibility_id;

  private UUID relation_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
