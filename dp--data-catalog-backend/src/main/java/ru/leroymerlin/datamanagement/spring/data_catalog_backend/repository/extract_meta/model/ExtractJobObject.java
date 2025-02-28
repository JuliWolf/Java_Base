package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobObjectStatus;

/**
 * @author juliwolf
 */

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "extract_job_object", schema = "extract_meta")
public class ExtractJobObject {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "job_object_id", updatable = false, nullable = false)
  private UUID jobObjectId;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "job_id", referencedColumnName = "job_id")
  @ToString.Exclude
  private ExtractJob extractJob;

  @Column(name="key_timestamp")
  private java.sql.Timestamp keyTimestamp;

  @Column(name = "kafka_topic", columnDefinition = "varchar(255)")
  private String kafkaTopic;

  @Column(name = "table_stage_name", columnDefinition = "varchar(255)")
  private String tableStageName;

  @Column(name = "control_sum", columnDefinition = "bigint")
  private Long controlSum;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_object_status", columnDefinition = "varchar(12)")
  private JobObjectStatus jobObjectStatus;

  @Column(name = "job_object_error", columnDefinition = "text")
  private String jobObjectError;

  @Column(name = "created_datetime")
  @CreationTimestamp
  private java.sql.Timestamp createdDatetime;

  public ExtractJobObject (
    ExtractJob extractJob,
    java.sql.Timestamp keyTimestamp,
    Long controlSum,
    String kafkaTopic,
    String tableStageName
  ) {
    this.extractJob = extractJob;
    this.jobObjectStatus = JobObjectStatus.NEW;
    this.keyTimestamp = keyTimestamp;
    this.controlSum = controlSum;
    this.kafkaTopic = kafkaTopic;
    this.tableStageName = tableStageName;
  }
}
