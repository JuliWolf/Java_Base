package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class AssetTypeStatusAssignmentNotFoundException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public AssetTypeStatusAssignmentNotFoundException () {
    super("asset type status assignment not found");
  }

  public AssetTypeStatusAssignmentNotFoundException (UUID assetTypeStatusAssignmentId) {
    super("Asset type - status assignment '" + assetTypeStatusAssignmentId + "' not found");
  }

  public AssetTypeStatusAssignmentNotFoundException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
