package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtractFilters.models.MetadataExtractFilterAirflowData;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.metadataExtracts.models.MetadataExtractAirflowData;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;

/**
 * @author juliwolf
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetMetadataExtractResponse implements Response {
  private UUID metadata_extract_id;

  private UUID root_asset_id;

  private String root_asset_name;

  private String extract_schedule_cron;

  private MetadataExtractStatus extract_status;

  private String airflow_dag;

  private String kafka_topic;

  private MetadataSourceKind source_kind;

  private MetadataSourceType source_type;

  private Map<String, Object> connection_info;

  private Map<String, Object> vault_secrets;

  private Boolean full_meta_flag;

  private List<GetMetadataExtractFilterResponse> metadata_extract_filters;

  public GetMetadataExtractResponse (
    MetadataExtractAirflowData metadataExtract,
    List<MetadataExtractFilterAirflowData> filters
  ) {
    this.metadata_extract_id = metadataExtract.getMetadataExtractId();
    this.root_asset_id = metadataExtract.getRootAssetId();
    this.root_asset_name = metadataExtract.getRootAssetName();
    this.extract_schedule_cron = metadataExtract.getExtractScheduleCron();
    this.extract_status = metadataExtract.getExtractStatus();
    this.airflow_dag = metadataExtract.getAirflowDag();
    this.kafka_topic = metadataExtract.getKafkaTopic();
    this.source_kind = metadataExtract.getSourceKind();
    this.source_type = metadataExtract.getSourceType();
    this.full_meta_flag = metadataExtract.getFullMetaFlag();

    setVaultSecret(metadataExtract.getVaultSecrets());
    setConnectionInfo(metadataExtract.getConnectionInfo());

    this.metadata_extract_filters = filters != null
      ? filters.stream().map(filter -> new GetMetadataExtractFilterResponse(
          filter.getMetadataExtractFilterId(),
          filter.getFilterType(),
          filter.getObjectType(),
          filter.getConditionType(),
          filter.getValue()
        )).toList()
      : new ArrayList<>();
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GetMetadataExtractFilterResponse implements Response {
    private UUID metadata_extract_filter_id;

    private MetadataFilterType filter_type;

    private String object_type;

    private ConditionType condition_type;

    private String value;
  }

  private void setVaultSecret (String vaultSecretString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      this.vault_secrets = objectMapper.readValue(vaultSecretString, Map.class);
    } catch (JsonProcessingException e) {
      this.vault_secrets = null;
    }
  }

  private void setConnectionInfo (String connectionInfoString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      this.connection_info = objectMapper.readValue(connectionInfoString, Map.class);
    } catch (JsonProcessingException e) {
      this.connection_info = null;
    }
  }
}
