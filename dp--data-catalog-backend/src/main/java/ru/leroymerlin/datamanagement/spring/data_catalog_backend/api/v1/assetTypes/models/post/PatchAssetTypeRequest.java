package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatchAssetTypeRequest implements Request {
  private Optional<String> asset_type_name;

  private Optional<String> asset_type_description;

  private String asset_type_acronym;

  private String asset_type_color;

  private Optional<String> asset_name_validation_mask;

  private Optional<String> asset_name_validation_mask_example;
}
