package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class DeleteAssetRequest implements Request {
  private String asset_id;
}
