package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post;

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
@Setter
@Getter
public class GetAssetHeaderBusinessOwnerResponse implements Response {
  private UUID responsibility_id;

  private ResponsibleType responsible_type;

  private UUID responsible_id;

  private String responsible_name;

  private String responsible_fullname;
}
