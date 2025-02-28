package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class AssetTypeCardHeaderAssignmentNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public AssetTypeCardHeaderAssignmentNotFoundException () {
    super("asset type card header assignment not found");
  }

  public AssetTypeCardHeaderAssignmentNotFoundException (UUID assetTypeId) {
    super("Asset type card header assignment with id " + assetTypeId + " not found");
  }

  public AssetTypeCardHeaderAssignmentNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
