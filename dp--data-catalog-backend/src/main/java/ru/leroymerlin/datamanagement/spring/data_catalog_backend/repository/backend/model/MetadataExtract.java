package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataExtractStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataSourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataSourceType;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "metadata_extract", schema = "public")
public class MetadataExtract {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "metadata_extract_id", updatable = false, nullable = false)
  private UUID metadataExtractId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_kind", columnDefinition = "varchar(12)")
  private MetadataSourceKind metadataSourceKind;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", columnDefinition = "varchar(12)")
  private MetadataSourceType metadataSourceType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "root_asset_id", referencedColumnName = "asset_id")
  @ToString.Exclude
  private Asset asset;

  @Column(name = "kafka_topic", columnDefinition = "varchar(255)")
  private String kafkaTopic;

  @Column(name = "airflow_dag", columnDefinition = "varchar(100)")
  private String airflowDag;

  @Column(name="connection_info", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String connectionInfo;

  @Column(name="vault_secrets", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String vaultSecrets;

  @Column(name = "full_meta_flag")
  private Boolean fullMetaFlag;

  @Column(name="extract_schedule_cron", columnDefinition = "varchar(255)")
  private String extractScheduleCron;

  @Enumerated(EnumType.STRING)
  @Column(name="extract_status", columnDefinition = "varchar(3)")
  private MetadataExtractStatus extractStatus;

  @Column(name = "created_on")
  @CreationTimestamp
  private java.sql.Timestamp createdOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User createdBy;

  @Column(name = "last_modified_on")
  private java.sql.Timestamp lastModifiedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_modified_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User modifiedBy;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private java.sql.Timestamp deletedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User deletedBy;

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public MetadataExtract (
    MetadataSourceKind metadataSourceKind,
    MetadataSourceType metadataSourceType,
    Asset asset,
    String kafkaTopic,
    String airflowDag,
    String connectionInfo,
    String vaultSecrets,
    Boolean fullMetaFlag,
    String extractScheduleCron,
    MetadataExtractStatus extractStatus,
    User createdBy
  ) {
    this.metadataSourceKind = metadataSourceKind;
    this.metadataSourceType = metadataSourceType;
    this.asset = asset;
    this.kafkaTopic = kafkaTopic;
    this.airflowDag = airflowDag;
    this.connectionInfo = connectionInfo;
    this.vaultSecrets = vaultSecrets;
    this.fullMetaFlag = fullMetaFlag;
    this.extractScheduleCron = extractScheduleCron;
    this.extractStatus = extractStatus;
    this.createdBy = createdBy;
  }
}
