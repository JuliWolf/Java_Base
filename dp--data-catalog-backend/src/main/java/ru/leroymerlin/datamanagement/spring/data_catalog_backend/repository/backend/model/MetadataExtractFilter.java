package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ConditionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataFilterType;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "metadata_extract_filter", schema = "public")
public class MetadataExtractFilter {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "metadata_exract_filter_id", updatable = false, nullable = false)
  private UUID metadataExtractFilterId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "metadata_extract_id", referencedColumnName = "metadata_extract_id")
  @ToString.Exclude
  private MetadataExtract metadataExtract;

  @Enumerated(EnumType.STRING)
  @Column(name = "filter_type", columnDefinition = "varchar(10)")
  private MetadataFilterType metadataFilterType;

  @Column(name = "object_type", columnDefinition = "varchar(10)")
  private String objectType;

  @Enumerated(EnumType.STRING)
  @Column(name = "condition_type", columnDefinition = "varchar(12)")
  private ConditionType conditionType;

  @Column(name = "value", columnDefinition = "text")
  private String value;

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

  public MetadataExtractFilter (
    MetadataExtract metadataExtract,
    MetadataFilterType metadataFilterType,
    String objectType,
    ConditionType conditionType,
    String value,
    User createdBy
  ) {
    this.metadataExtract = metadataExtract;
    this.metadataFilterType = metadataFilterType;
    this.objectType = objectType;
    this.conditionType = conditionType;
    this.value = value;
    this.createdBy = createdBy;
  }
}
