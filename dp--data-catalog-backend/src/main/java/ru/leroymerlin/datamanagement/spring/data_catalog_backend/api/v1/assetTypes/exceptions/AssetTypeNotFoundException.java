package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class AssetTypeNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public AssetTypeNotFoundException() {
    super("asset type not found");
  }

  public AssetTypeNotFoundException(UUID assetTypeId) {
    super("Asset type with id " + assetTypeId + " not found");
  }

  public AssetTypeNotFoundException(Map<String, Object> details) {
    this();

    this.details = details;
  }
}
