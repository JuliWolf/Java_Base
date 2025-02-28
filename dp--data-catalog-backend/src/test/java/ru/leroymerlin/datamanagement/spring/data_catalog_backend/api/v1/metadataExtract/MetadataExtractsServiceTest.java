package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.MetadataExtractsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtractFilters.MetadataExtractFilterRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtracts.MetadataExtractRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.MetadataExtract;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.MetadataExtractFilter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractRequestParams;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author juliwolf
 */

public class MetadataExtractsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private MetadataExtractRepository metadataExtractRepository;

  @Autowired
  private MetadataExtractFilterRepository metadataExtractFilterRepository;

  @Autowired
  private MetadataExtractsService metadataExtractsService;

  Asset asset;
  AssetType assetType;

  @BeforeAll
  public void createRootAsset () {
    assetType = assetTypeRepository.save(new AssetType("asset type name", "description", "atn", "red", language, user));
    asset = assetRepository.save(new Asset("first asset", assetType, "displayed name", language, null, null, user));
  }

  @AfterEach
  public void clearCreatedData () {
    metadataExtractFilterRepository.deleteAll();
    metadataExtractRepository.deleteAll();
  }

  @AfterAll
  public void clearPreparedData () {
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @Test
  public void getMetadataExtractsSuccessIntegrationTest () {
    MetadataExtract firstExtract = metadataExtractRepository.save(new MetadataExtract(MetadataSourceKind.RELATIONAL, MetadataSourceType.POSTGRESQL, asset, "canon_topic", "airflow_234", "{\"connection_info\":\"info\"}", "{\"secrets\":\"some secrets\"}", true, "* * * * * *", MetadataExtractStatus.ON, user));
    MetadataExtract secondExtract = metadataExtractRepository.save(new MetadataExtract(MetadataSourceKind.API, MetadataSourceType.POSTGRESQL, asset, "canon_schema", "airflow_435", "{\"connection_info\":\"info\"}", "{\"secrets\":\"some secrets\"}", true, "* * * * * *", MetadataExtractStatus.ON, user));
    MetadataExtract thirdExtract = metadataExtractRepository.save(new MetadataExtract(MetadataSourceKind.API, MetadataSourceType.MSSQL, asset, "canon_schema", "airflow_435", "{\"connection_info\":\"info\"}", "{\"secrets\":\"some secrets\"}", true, "* * * * * *", MetadataExtractStatus.OFF, user));
    MetadataExtract forthExtract = metadataExtractRepository.save(new MetadataExtract(MetadataSourceKind.KV, MetadataSourceType.MONGO, asset, "canon_schema", "airflow_435", "{\"connection_info\":\"info\"}", "{\"secrets\":\"some secrets\"}", true, "* * * * * *", MetadataExtractStatus.ON, user));

    MetadataExtractFilter firstExtractFirstFilter = metadataExtractFilterRepository.save(new MetadataExtractFilter(firstExtract, MetadataFilterType.INCLUDE, "10", ConditionType.EQ, "some value", user));
    MetadataExtractFilter firstExtractSecondFilter = metadataExtractFilterRepository.save(new MetadataExtractFilter(firstExtract, MetadataFilterType.EXCLUDE, "11", ConditionType.LIKE, "some value", user));
    MetadataExtractFilter thirdExtractFirstFilter = metadataExtractFilterRepository.save(new MetadataExtractFilter(thirdExtract, MetadataFilterType.INCLUDE, "34t", ConditionType.LIKE, "another value", user));
    MetadataExtractFilter forthExtractFirstFilter = metadataExtractFilterRepository.save(new MetadataExtractFilter(forthExtract, MetadataFilterType.EXCLUDE, "23", ConditionType.REGEXP, "another value", user));

    assertAll(
      () -> assertEquals(4, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, null, null, null)).getResults().size(), "find all"),
      () -> assertEquals(1, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, MetadataSourceKind.RELATIONAL, null, null, null)).getResults().size(), "source kind relational"),
      () -> assertEquals(2, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, MetadataSourceKind.RELATIONAL, null, null, null)).getResults().get(0).getMetadata_extract_filters().size(), "source kind relational filters size"),
      () -> assertEquals(1, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, MetadataSourceKind.KV, null, null, null)).getResults().size(), "source kind kv"),
      () -> assertEquals(1, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, MetadataSourceKind.KV, null, null, null)).getResults().get(0).getMetadata_extract_filters().size(), "source kind kv filters size"),
      () -> assertEquals(0, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, MetadataSourceKind.KV, MetadataSourceType.MSSQL, null, null)).getResults().size(), "source kind kv and source type MSSQL"),
      () -> assertEquals(1, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, MetadataSourceType.MSSQL, null, null)).getResults().size(), "source type MSSQL"),
      () -> assertEquals(1, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, MetadataSourceType.MSSQL, null, null)).getResults().get(0).getMetadata_extract_filters().size(), "source type MSSQL filters size"),
      () -> assertEquals(2, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, MetadataSourceType.POSTGRESQL, null, null)).getResults().size(), "source type POSTGRESQL"),
      () -> assertEquals(2, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, MetadataSourceType.POSTGRESQL, MetadataExtractStatus.ON, null)).getResults().size(), "source type POSTGRESQL and extract status on"),
      () -> assertEquals(0, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, MetadataSourceType.POSTGRESQL, MetadataExtractStatus.OFF, null)).getResults().size(), "source type POSTGRESQL and extract status off"),
      () -> assertEquals(1, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, null, MetadataExtractStatus.OFF, null)).getResults().size(), "extract status off"),
      () -> assertEquals(1, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, null, MetadataExtractStatus.OFF, true)).getResults().size(), "extract status off and full meta flag true"),
      () -> assertEquals(4, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, null, null, true)).getResults().size(), "full meta flag true"),
      () -> assertEquals(0, metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(50, 0, null, null, null, false)).getResults().size(), "full meta flag false")
    );
  }
}
