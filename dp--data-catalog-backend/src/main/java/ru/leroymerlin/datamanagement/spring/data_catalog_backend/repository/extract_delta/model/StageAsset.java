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

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(
  name = "stage_asset",
  schema = "extract_delta"
)
public class StageAsset {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stage_asset_id", updatable = false, nullable = false)
  private Long stageAssetId;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "job_id", referencedColumnName = "job_id")
  @ToString.Exclude
  private ExtractJob extractJob;

  @Column(name="asset_name", columnDefinition = "text")
  private String assetName;

  @Column(name = "asset_type_id")
  private UUID assetTypeId;

  @Column(name="asset_displayname", columnDefinition = "text")
  private String assetDisplayName;

  @Column(name="lifecycle_status")
  private UUID lifecycleStatusId;

  @Column(name="stewardship_status")
  private UUID stewardshipStatusId;

  @Column(name="inserted_datetime")
  @CreationTimestamp
  private java.sql.Timestamp insertedDatetime;

  @Enumerated(EnumType.STRING)
  @Column(name="action_decision", columnDefinition = "varchar(1)")
  private ActionDecision actionDecision;

  @Enumerated(EnumType.STRING)
  @Column(name="action_process_status", columnDefinition = "varchar(25)")
  private ActionProcessStatus actionProcessStatus;

  @Column(name="matched_asset_id")
  private UUID matchedAssetId;

  @Column(name="created_asset_id")
  private UUID createdAssetId;

  @Column(name="action_processed_datetime")
  private java.sql.Timestamp actionProcessedDatetime;

  public StageAsset (ExtractJob extractJob, String assetName, UUID assetTypeId, String assetDisplayName, UUID lifecycleStatusId, UUID stewardshipStatusId, ActionDecision actionDecision) {
    this.extractJob = extractJob;
    this.assetName = assetName;
    this.assetTypeId = assetTypeId;
    this.assetDisplayName = assetDisplayName;
    this.lifecycleStatusId = lifecycleStatusId;
    this.stewardshipStatusId = stewardshipStatusId;
    this.actionDecision = actionDecision;
  }
}
