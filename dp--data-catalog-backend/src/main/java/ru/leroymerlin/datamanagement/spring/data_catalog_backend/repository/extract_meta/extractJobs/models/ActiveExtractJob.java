package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ActiveExtractJob {
  private UUID jobId;

  private String kafkaTopic;

  private Integer tablesCount;

  private SourceType sourceType;

  private String sourceName;

  private JobStatus jobStatus;

  private Boolean isFullMeta;

  private UUID rootAssetId;

  private java.sql.Timestamp lastStatusChangeDatetime;
}
