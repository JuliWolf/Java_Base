package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;

/**
 * @author JuliWolf
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGlobalResponsibilityRequest implements Request {
  private String responsible_id;

  private ResponsibleType responsible_type;

  private String role_id;
}
