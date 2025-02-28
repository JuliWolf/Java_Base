package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtractFilters.MetadataExtractFilterRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtractFilters.models.MetadataExtractFilterAirflowData;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtracts.MetadataExtractRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtracts.models.MetadataExtractAirflowData;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractRequestParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractsResponse;

/**
 * @author juliwolf
 */

@Service
public class MetadataExtractsServiceImpl implements MetadataExtractsService {
  private final MetadataExtractRepository metadataExtractRepository;

  private final MetadataExtractFilterRepository metadataExtractFilterRepository;

  public MetadataExtractsServiceImpl (
    MetadataExtractRepository metadataExtractRepository,
    MetadataExtractFilterRepository metadataExtractFilterRepository
  ) {
    this.metadataExtractRepository = metadataExtractRepository;
    this.metadataExtractFilterRepository = metadataExtractFilterRepository;
  }

  @Override
  public GetMetadataExtractsResponse getMetadataExtracts (
    GetMetadataExtractRequestParams params
  ) {
    Integer pageSize = PageableUtils.getPageSize(params.getPageSize());
    Integer pageNumber = PageableUtils.getPageNumber(params.getPageNumber());

    Page<MetadataExtractAirflowData> metadataExtractsResponses = metadataExtractRepository.findAllByParams(
      params.getMetadataSourceKind(),
      params.getMetadataSourceType(),
      params.getExtractStatus(),
      params.getFullMetaFlag(),
      PageRequest.of(pageNumber, pageSize, Sort.by("metadataExtractId").ascending())
    );

    List<UUID> metadataExtractUUIDs = metadataExtractsResponses.stream().map(MetadataExtractAirflowData::getMetadataExtractId).toList();
    List<MetadataExtractFilterAirflowData> metadataFilters = metadataExtractFilterRepository.findAllByExtractIds(metadataExtractUUIDs);
    Map<UUID, List<MetadataExtractFilterAirflowData>> filtersByExtractMetadata = metadataFilters.stream().collect(Collectors.groupingBy(MetadataExtractFilterAirflowData::getMetadataExtractId));

    List<GetMetadataExtractResponse> response = metadataExtractsResponses.map(extract -> {
      List<MetadataExtractFilterAirflowData> filters = filtersByExtractMetadata.get(extract.getMetadataExtractId());

      return new GetMetadataExtractResponse(extract, filters);
    }).toList();


    return new GetMetadataExtractsResponse(
      metadataExtractsResponses.getTotalElements(),
      pageSize,
      pageNumber,
      response
    );
  }
}
