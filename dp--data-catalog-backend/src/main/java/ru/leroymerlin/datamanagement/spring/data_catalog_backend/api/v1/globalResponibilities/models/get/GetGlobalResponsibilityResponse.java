package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get;

import java.util.UUID;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class GetGlobalResponsibilityResponse implements Response {
  private UUID global_responsibility_id;

  private UUID responsible_id;

  private String responsible_name;

  private ResponsibleType responsible_type;

  private UUID role_id;

  private String role_name;

  private String role_description;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
