package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract;

import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractRequestParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractsResponse;

/**
 * @author juliwolf
 */

public interface MetadataExtractsService {
  GetMetadataExtractsResponse getMetadataExtracts (GetMetadataExtractRequestParams params);
}
