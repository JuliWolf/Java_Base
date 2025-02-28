package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;

/**
 * @author JuliWolf
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostGlobalResponsibilityResponse implements Response {
  private UUID global_responsibility_id;

  private UUID responsible_id;

  private ResponsibleType responsible_type;

  private UUID role_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;

}
