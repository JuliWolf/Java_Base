package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.get;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.models.ActiveExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetJobsResponse implements Response {
  private List<GetJobResponse> jobs;

  public GetJobsResponse (Page<ActiveExtractJob> extractJobs) {
    this.jobs = extractJobs.stream()
      .map(job -> new GetJobResponse(
        job.getJobId(),
        job.getKafkaTopic(),
        job.getTablesCount(),
        job.getSourceType(),
        job.getSourceName(),
        job.getJobStatus(),
        job.getIsFullMeta(),
        job.getRootAssetId(),
        job.getLastStatusChangeDatetime()
      )).collect(Collectors.toList());
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class GetJobResponse implements Response {
    private UUID job_id;

    private String kafka_topic;

    private Integer tables_count;

    private SourceType source_type;

    private String source_name;

    private JobStatus job_status;

    private Boolean full_meta_flag;

    private UUID root_asset_id;

    private java.sql.Timestamp last_status_change_datetime;
  }
}
