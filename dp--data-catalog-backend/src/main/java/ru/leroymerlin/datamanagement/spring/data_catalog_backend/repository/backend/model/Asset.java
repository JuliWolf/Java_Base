package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.sql.Timestamp;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "asset", schema = "public")
public class Asset {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "asset_id", updatable = false, nullable = false)
  private UUID assetId;

  @Column(name="asset_name")
  private String assetName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  private AssetType assetType;

  @Column(name="asset_displayname")
  private String assetDisplayName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_language", referencedColumnName = "language_id")
  @ToString.Exclude
  private Language language;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lifecycle_status", referencedColumnName = "status_id")
  @ToString.Exclude
  private Status lifecycleStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stewardship_status", referencedColumnName = "status_id")
  @ToString.Exclude
  private Status stewardshipStatus;

  @Column(name = "created_on")
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

  public UUID getLastModifiedByUUID () {
    return modifiedBy != null ? modifiedBy.getUserId() : null;
  }

  public UUID getDeletedByUUID () {
    return deletedBy != null ? deletedBy.getUserId() : null;
  }

  public String getLanguageName () {
    return language != null ? language.getLanguage() : null;
  }

  public Asset (String assetName, AssetType assetType, String assetDisplayName, Language language, Status lifecycleStatus, Status stewardshipStatus, User createdBy) {
    this.assetName = assetName;
    this.assetType = assetType;
    this.assetDisplayName = assetDisplayName;
    this.language = language;
    this.lifecycleStatus = lifecycleStatus;
    this.stewardshipStatus = stewardshipStatus;
    this.createdBy = createdBy;
    this.createdOn = new Timestamp(System.currentTimeMillis());
  }
}
