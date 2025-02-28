package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions;

import java.util.UUID;

public class StatusIsUsedForAssetException extends RuntimeException {
  public StatusIsUsedForAssetException (UUID assetId) {
    super("This status is still used for asset '" + assetId + "'");
  }
}
