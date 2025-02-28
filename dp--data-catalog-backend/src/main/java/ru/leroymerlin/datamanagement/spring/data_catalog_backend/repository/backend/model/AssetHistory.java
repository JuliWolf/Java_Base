package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(
  name = "asset_history", schema = "public",
  indexes = @Index(name = "idx_asset_id_valid_from_valid_to", columnList = "asset_id, valid_from, valid_to")
)
public class AssetHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "asset_history_id", updatable = false, nullable = false)
  private UUID assetHistoryId;

  @Column(name="asset_id")
  private UUID assetId;

  @Column(name="asset_name")
  private String assetName;

  @Column(name="asset_type_id")
  private UUID assetTypeId;

  @Column(name="asset_displayname")
  private String assetDisplayName;

  @Column(name="source_language")
  private UUID languageId;

  @Column(name="lifecycle_status")
  private UUID lifecycleStatusId;

  @Column(name="stewardship_status")
  private UUID stewardshipStatusId;

  @Column(name = "created_on")
  private java.sql.Timestamp createdOn;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "last_modified_on")
  private java.sql.Timestamp lastModifiedOn;

  @Column(name = "last_modified_by")
  private UUID lastModifiedBy;

  @Column(name="deleted_flag")
  private Boolean isDeleted;

  @Column(name = "deleted_on")
  private java.sql.Timestamp deletedOn;

  @Column(name = "deleted_by")
  private UUID deletedBy;

  @Column(name = "valid_from")
  private java.sql.Timestamp validFrom;

  @Column(name = "valid_to")
  private java.sql.Timestamp validTo;

  public AssetHistory (Asset asset) {
    this.assetId = asset.getAssetId();
    this.assetName = asset.getAssetName();
    this.assetTypeId = asset.getAssetType().getAssetTypeId();
    this.assetDisplayName = asset.getAssetDisplayName();
    this.languageId = asset.getLanguage() != null ? asset.getLanguage().getLanguageId() : null;
    this.lifecycleStatusId = asset.getLifecycleStatus() != null ? asset.getLifecycleStatus().getStatusId() : null;
    this.stewardshipStatusId = asset.getStewardshipStatus() != null ? asset.getStewardshipStatus().getStatusId() : null;
    this.createdOn = asset.getCreatedOn();
    this.createdBy = asset.getCreatedByUUID();
    this.lastModifiedOn = asset.getLastModifiedOn();
    this.lastModifiedBy = asset.getLastModifiedByUUID();
    this.isDeleted = asset.getIsDeleted();
    this.deletedOn = asset.getDeletedOn();
    this.deletedBy = asset.getDeletedByUUID();
  }

  public void setCreatedValidDate () {
    this.validFrom = this.createdOn;
    this.validTo = HistoryDateUtils.getValidToDefaultTime();
  }

  public void setUpdatedValidDate () {
    this.validFrom = this.lastModifiedOn;
    this.validTo = HistoryDateUtils.getValidToDefaultTime();
  }

  public void setDeletedValidDate () {
    this.validFrom = this.deletedOn;
    this.validTo = this.deletedOn;
  }
}
