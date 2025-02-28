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
@Table(name = "stage_relation", schema = "extract_delta")
public class StageRelation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stage_relation_id", updatable = false, nullable = false)
  private Long stageRelationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "job_id", referencedColumnName = "job_id")
  @ToString.Exclude
  private ExtractJob extractJob;

  @Column(name="relation_type_id")
  private UUID relationTypeId;

  @Column(name="relation_type_component_1_id")
  private UUID relationTypeComponent1Id;

  @Column(name="asset_1_id")
  private UUID asset1Id;

  @Column(name="asset_name_1_nk", columnDefinition = "text")
  private String assetName1Nk;

  @Column(name="relation_type_component_2_id")
  private UUID relationTypeComponent2Id;

  @Column(name="asset_2_id")
  private UUID asset2Id;

  @Column(name="asset_name_2_nk", columnDefinition = "text")
  private String assetName2Nk;

  @Column(name="inserted_datetime")
  @CreationTimestamp
  private java.sql.Timestamp insertedDatetime;

  @Enumerated(EnumType.STRING)
  @Column(name="action_decision", columnDefinition = "varchar(1)")
  private ActionDecision actionDecision;

  @Enumerated(EnumType.STRING)
  @Column(name="action_process_status")
  private ActionProcessStatus actionProcessStatus;

  @Column(name="matched_relation_id")
  private UUID matchedRelationId;

  @Column(name="created_relation_id")
  private UUID createdRelationId;

  @Column(name="action_processed_datetime")
  private java.sql.Timestamp actionProcessedDatetime;

  public StageRelation (ExtractJob extractJob, UUID relationTypeId, UUID relationTypeComponent1Id, UUID asset1Id, UUID relationTypeComponent2Id, UUID asset2Id, ActionDecision actionDecision) {
    this.extractJob = extractJob;
    this.relationTypeId = relationTypeId;
    this.relationTypeComponent1Id = relationTypeComponent1Id;
    this.asset1Id = asset1Id;
    this.relationTypeComponent2Id = relationTypeComponent2Id;
    this.asset2Id = asset2Id;
    this.actionDecision = actionDecision;
  }
}
