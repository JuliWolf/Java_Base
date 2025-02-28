package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.sql.Timestamp;
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
@Table(name = "asset_link_usage", schema = "public")
public class AssetLinkUsage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "asset_link_usage_id", updatable = false, nullable = false)
  private UUID assetLinkUsageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_id", referencedColumnName = "attribute_id")
  @ToString.Exclude
  private Attribute attribute;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "link_asset_id", referencedColumnName = "asset_id")
  @ToString.Exclude
  private Asset asset;

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

  public AssetLinkUsage (Attribute attribute, Asset asset, User createdBy) {
    this.attribute = attribute;
    this.asset = asset;
    this.createdBy = createdBy;
    this.createdOn = new Timestamp(System.currentTimeMillis());
  }
}
