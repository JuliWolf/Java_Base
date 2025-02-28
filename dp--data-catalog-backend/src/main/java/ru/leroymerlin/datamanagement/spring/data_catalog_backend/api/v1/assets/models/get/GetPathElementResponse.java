package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetPathElementResponse {
  private String asset_display_name;

  private String asset_name;

  private UUID asset_id;
}
