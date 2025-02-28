package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionProcessStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;

/**
 * @author juliwolf
 */

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(
  name = "stage_attribute",
  schema = "extract_delta",
  uniqueConstraints=@UniqueConstraint(
          columnNames={ "asset_name_nk", "job_id" }
  )
)
public class StageAttribute {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stage_attribute_id", updatable = false, nullable = false)
  private Long stageAttributeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "job_id", referencedColumnName = "job_id")
  @ToString.Exclude
  private ExtractJob extractJob;

  @Column(name="nk", columnDefinition = "text")
  private String nk;

  @Column(name="asset_name_nk", columnDefinition = "text")
  private String assetNameNk;

  @Column(name = "asset_id")
  private UUID assetId;

  @Column(name = "attribute_type_id")
  private UUID attributeTypeId;

  @Column(name="value", columnDefinition = "text")
  private String value;

  @Column(name="inserted_datetime")
  @CreationTimestamp
  private java.sql.Timestamp insertedDatetime;

  @Enumerated(EnumType.STRING)
  @Column(name="action_decision", columnDefinition = "varchar(1)")
  private ActionDecision actionDecision;

  @Enumerated(EnumType.STRING)
  @Column(name="action_process_status")
  private ActionProcessStatus actionProcessStatus;

  @Column(name="matched_attribute_id")
  private UUID matchedAttributeId;

  @Column(name="created_attribute_id")
  private UUID createdAttributeId;

  @Column(name="action_processed_datetime")
  private java.sql.Timestamp actionProcessedDatetime;

  public StageAttribute (ExtractJob extractJob, UUID assetId, UUID attributeTypeId, String value, ActionDecision actionDecision) {
    this.extractJob = extractJob;
    this.assetId = assetId;
    this.attributeTypeId = attributeTypeId;
    this.value = value;
    this.actionDecision = actionDecision;
  }
}
