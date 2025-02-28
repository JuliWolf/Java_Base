package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class AssetNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public AssetNotFoundException () {
    super("asset not found");
  }

  public AssetNotFoundException (UUID assetTypeId) {
    super("Asset with id '" + assetTypeId + "' not found");
  }

  public AssetNotFoundException(Map<String, Object> details) {
    this();

    this.details = details;
  }
}
