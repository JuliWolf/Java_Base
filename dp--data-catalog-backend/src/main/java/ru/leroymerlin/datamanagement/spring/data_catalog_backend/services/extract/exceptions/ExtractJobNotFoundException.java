package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.exceptions;

import java.util.UUID;

/**
 * @author juliwolf
 */

public class ExtractJobNotFoundException extends RuntimeException {
  public ExtractJobNotFoundException () {
    super("extract job not found");
  }

  public ExtractJobNotFoundException (UUID extractJobId) {
    super("Extract job view with id " + extractJobId + " not found");
  }
}
