package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
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
@Table(name = "stage_responsibility", schema = "extract_delta")
public class StageResponsibility {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stage_responsibility_id", updatable = false, nullable = false)
  private Long stageResponsibilityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "job_id", referencedColumnName = "job_id")
  @ToString.Exclude
  private ExtractJob extractJob;

  @Column(name="nk", columnDefinition = "text")
  private String nk;

  @Column(name="role_id")
  private UUID roleId;

  @Column(name="asset_id")
  private UUID assetId;

  @Column(name="user_id")
  private UUID userId;

  @Column(name="group_id")
  private UUID groupId;

  @Enumerated(EnumType.STRING)
  @Column(name="responsible_type", columnDefinition = "varchar(10)")
  private ResponsibleType responsibleType;

  @Column(name="asset_name_nk", columnDefinition = "text")
  private String assetNameNk;

  @Column(name="inserted_datetime")
  @CreationTimestamp
  private java.sql.Timestamp insertedDatetime;

  @Enumerated(EnumType.STRING)
  @Column(name="action_decision", columnDefinition = "varchar(1)")
  private ActionDecision actionDecision;

  @Enumerated(EnumType.STRING)
  @Column(name="action_process_status")
  private ActionProcessStatus actionProcessStatus;

  @Column(name="matched_responsibility_id")
  private UUID matchedResponsibilityId;

  @Column(name="created_responsibility_id")
  private UUID createdResponsibilityId;

  @Column(name="action_processed_datetime")
  private java.sql.Timestamp actionProcessedDatetime;

  public StageResponsibility (
    ExtractJob extractJob,
    UUID roleId,
    UUID assetId,
    UUID userId,
    UUID groupId,
    ResponsibleType responsibleType,
    ActionDecision actionDecision
  ) {
    this.extractJob = extractJob;
    this.roleId = roleId;
    this.assetId = assetId;
    this.userId = userId;
    this.groupId = groupId;
    this.responsibleType = responsibleType;
    this.actionDecision = actionDecision;
  }
}
