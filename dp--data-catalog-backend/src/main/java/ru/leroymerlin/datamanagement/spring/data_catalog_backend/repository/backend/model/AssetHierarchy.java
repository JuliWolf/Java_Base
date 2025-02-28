package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "asset_hierarchy", schema = "public")
public class AssetHierarchy {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "asset_hierarchy_id", updatable = false, nullable = false)
  private UUID assetHierarchyId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_asset_id", referencedColumnName = "asset_id")
  @ToString.Exclude
  private Asset parentAsset;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "child_asset_id", referencedColumnName = "asset_id")
  @ToString.Exclude
  private Asset childAsset;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_id", referencedColumnName = "relation_id")
  @ToString.Exclude
  private Relation relation;

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

  public AssetHierarchy (Asset parentAsset, Asset childAsset, Relation relation, User createdBy) {
    this.parentAsset = parentAsset;
    this.childAsset = childAsset;
    this.relation = relation;
    this.createdBy = createdBy;
  }
}
