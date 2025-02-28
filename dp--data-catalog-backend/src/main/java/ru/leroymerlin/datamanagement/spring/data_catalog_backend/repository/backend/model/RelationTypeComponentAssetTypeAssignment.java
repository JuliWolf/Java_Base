package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "relation_type_component_asset_type_assignment", schema = "public")
public class RelationTypeComponentAssetTypeAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "relation_type_component_asset_type_assignment_id", updatable = false, nullable = false)
  private UUID relationTypeComponentAssetTypeAssignmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_type_component_id", referencedColumnName = "relation_type_component_id")
  @ToString.Exclude
  private RelationTypeComponent relationTypeComponent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  private AssetType assetType;

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

  public RelationTypeComponentAssetTypeAssignment (RelationTypeComponent relationTypeComponent, AssetType assetType, Boolean isInherited, AssetType parentAssetType, User createdBy) {
    this.relationTypeComponent = relationTypeComponent;
    this.assetType = assetType;
    this.isInherited = isInherited;
    this.parentAssetType = parentAssetType;
    this.createdBy = createdBy;
  }
}
