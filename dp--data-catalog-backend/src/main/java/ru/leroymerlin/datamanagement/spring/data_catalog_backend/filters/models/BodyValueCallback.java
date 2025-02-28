package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.models;

import java.io.IOException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;

/**
 * @author juliwolf
 */

@FunctionalInterface
public interface BodyValueCallback {
  void parseBody (byte[] body, RequestValues requestValues, AuthUserDetails userDetails) throws IOException;
}
