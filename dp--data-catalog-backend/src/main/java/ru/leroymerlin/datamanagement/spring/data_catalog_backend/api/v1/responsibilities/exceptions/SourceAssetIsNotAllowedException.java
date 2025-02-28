package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author juliwolf
 */

@Getter
public class SourceAssetIsNotAllowedException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public SourceAssetIsNotAllowedException () {
    super("Bulk post responsibilities is not allowed for SOURCE-assets.");
  }

  public SourceAssetIsNotAllowedException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
