package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "asset_type_status_assignment", schema = "public")
public class AssetTypeStatusAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "asset_type_status_assignment_id", updatable = false, nullable = false)
  private UUID assetTypeStatusAssignmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  private AssetType assetType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_type")
  private AssignmentStatusType assignmentStatusType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "status_id", referencedColumnName = "status_id")
  @ToString.Exclude
  private Status status;

  @Column(name="inherited_flag", columnDefinition = "boolean default false")
  private Boolean isInherited = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  private AssetType parentAssetType;

  @Column(name = "created_on")
  @CreationTimestamp
  private java.sql.Timestamp createdOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User createdBy;

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

  public AssetTypeStatusAssignment (AssetType assetType, AssignmentStatusType assignmentStatusType, Status status, User createdBy) {
    this.assetType = assetType;
    this.assignmentStatusType = assignmentStatusType;
    this.status = status;
    this.createdBy = createdBy;
  }
}
