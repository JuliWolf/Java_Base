package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceType;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "extract_job", schema = "extract_meta")
public class ExtractJob {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "job_id", updatable = false, nullable = false)
  private UUID jobId;

  @Column(name="key_timestamp")
  private java.sql.Timestamp keyTimestamp;

  @Column(name="kafka_topic", columnDefinition = "varchar(255)")
  private String kafkaTopic;

  @Column(name="tables_count")
  private Integer tablesCount;

  @Enumerated(EnumType.STRING)
  @Column(name="source_type", columnDefinition = "varchar(15)")
  private SourceType sourceType;

  @Enumerated(EnumType.STRING)
  @Column(name="source_kind", columnDefinition = "varchar(15)")
  private SourceKind sourceKind;

  @Column(name="source_name", columnDefinition = "text")
  private String sourceName;

  @Enumerated(EnumType.STRING)
  @Column(name="job_status", columnDefinition = "varchar(20)")
  private JobStatus jobStatus;

  @Column(name="job_error", columnDefinition = "varchar(4000)")
  private String jobError;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @JoinColumn(name = "root_asset_id", referencedColumnName = "asset_id")
  @ToString.Exclude
  private Asset rootAsset;

  @Column(name = "created_datetime")
  @CreationTimestamp
  private java.sql.Timestamp createdDatetime;

  @Column(name="filter_criteria", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String filterCriteria;

  @Column(name="job_error_flag", columnDefinition = "boolean default false")
  private Boolean jobErrorFlag;

  @Column(name="full_meta_flag")
  private Boolean fullMetaFlag;

  @Column(name = "last_status_change_datetime")
  private java.sql.Timestamp lastStatusChangeDatetime;

  public ExtractJob (
    String kafkaTopic,
    java.sql.Timestamp keyTimestamp,
    Integer tablesCount,
    String sourceName,
    SourceKind sourceKind,
    SourceType sourceType,
    JobStatus jobStatus,
    Boolean fullMetaFlag,
    String filterCriteria
  ) {
    this.kafkaTopic = kafkaTopic;
    this.keyTimestamp = keyTimestamp;
    this.tablesCount = tablesCount;
    this.sourceName = sourceName;
    this.sourceKind = sourceKind;
    this.sourceType = sourceType;
    this.jobStatus = jobStatus;
    this.fullMetaFlag = fullMetaFlag;
    this.filterCriteria = filterCriteria;
  }

  public ExtractJob (
    String kafkaTopic,
    java.sql.Timestamp keyTimestamp,
    Integer tablesCount,
    SourceType sourceType,
    String sourceName,
    JobStatus jobStatus,
    Asset rootAsset,
    String filterCriteria,
    Boolean fullMetaFlag
  ) {
    this.keyTimestamp = keyTimestamp;
    this.kafkaTopic = kafkaTopic;
    this.tablesCount = tablesCount;
    this.sourceType = sourceType;
    this.sourceName = sourceName;
    this.jobStatus = jobStatus;
    this.rootAsset = rootAsset;
    this.filterCriteria = filterCriteria;
    this.fullMetaFlag = fullMetaFlag;
  }
}
