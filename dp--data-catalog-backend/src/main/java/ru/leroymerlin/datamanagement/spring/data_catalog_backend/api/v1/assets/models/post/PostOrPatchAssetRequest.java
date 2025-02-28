package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.interfaces.AssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PostOrPatchAssetRequest implements Request, AssetTypeRequest {
  private String asset_name;

  private String asset_displayname;

  private String asset_type_id;

  private String lifecycle_status;

  private String stewardship_status;
}
