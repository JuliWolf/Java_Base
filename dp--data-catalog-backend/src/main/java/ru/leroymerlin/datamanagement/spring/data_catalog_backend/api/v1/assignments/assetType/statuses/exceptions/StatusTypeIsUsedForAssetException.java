package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions;

import java.util.UUID;

public class StatusTypeIsUsedForAssetException extends RuntimeException {
  public StatusTypeIsUsedForAssetException (UUID assetId) {
    super("This status type is still used for asset '" + assetId + "'");
  }
}
