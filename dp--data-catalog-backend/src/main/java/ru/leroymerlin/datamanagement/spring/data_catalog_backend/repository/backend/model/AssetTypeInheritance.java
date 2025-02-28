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
@Table(name = "asset_type_inheritance", schema = "public")
public class AssetTypeInheritance {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "inheritance_id", updatable = false, nullable = false)
  private UUID inheritanceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  private AssetType parentAssetType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "child_asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  private AssetType childAssetType;

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

  public AssetTypeInheritance (AssetType parentAssetType, AssetType childAssetType, User createdBy) {
    this.parentAssetType = parentAssetType;
    this.childAssetType = childAssetType;
    this.createdBy = createdBy;
  }
}
