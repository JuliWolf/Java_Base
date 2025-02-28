package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post;

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
public class PostResponsibilityResponse implements Response {
  private UUID responsibility_id;

  private UUID asset_id;

  private UUID role_id;

  private ResponsibleType responsible_type;

  private UUID responsible_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
