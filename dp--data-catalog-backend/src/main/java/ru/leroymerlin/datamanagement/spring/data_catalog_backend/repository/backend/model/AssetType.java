package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "asset_type", schema = "public")
public class AssetType {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "asset_type_id", updatable = false, nullable = false)
  private UUID assetTypeId;

  @Column(name="asset_type_name", nullable = false)
  private String assetTypeName;

  @Column(name="asset_type_description")
  private String assetTypeDescription;

  @Column(name="asset_type_acronym", nullable = false)
  private String assetTypeAcronym;

  @Column(name="asset_type_color", nullable = false)
  private String assetTypeColor;

  @Column(name="asset_name_validation_mask", columnDefinition = "varchar(1000)")
  private String assetNameValidationMask;

  @Column(name="asset_name_validation_mask_example", columnDefinition = "text")
  private String assetNameValidationMaskExample;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_language", referencedColumnName = "language_id")
  @ToString.Exclude
  private Language language;

  @Column(name="is_translated", columnDefinition = "boolean default false")
  private Boolean isTranslated = false;

  @Column(name = "created_on")
  @CreationTimestamp
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

  public String getLanguageName () {
    return language != null ? language.getLanguage() : null;
  }

  public AssetType(
    String assetTypeName,
    String assetTypeDescription,
    String assetTypeAcronym,
    String assetTypeColor,
    String assetNameValidationMask,
    String assetNameValidationMaskExample,
    Language language,
    User createdBy
  ) {
    this.assetTypeName = assetTypeName;
    this.assetTypeDescription = assetTypeDescription;
    this.assetTypeAcronym = assetTypeAcronym;
    this.assetTypeColor = assetTypeColor;
    this.assetNameValidationMask = assetNameValidationMask;
    this.assetNameValidationMaskExample = assetNameValidationMaskExample;
    this.language = language;
    this.createdBy = createdBy;
  }

  public AssetType(
    String assetTypeName,
    String assetTypeDescription,
    String assetTypeAcronym,
    String assetTypeColor,
    Language language,
    User createdBy
  ) {
    this.assetTypeName = assetTypeName;
    this.assetTypeDescription = assetTypeDescription;
    this.assetTypeAcronym = assetTypeAcronym;
    this.assetTypeColor = assetTypeColor;
    this.language = language;
    this.createdBy = createdBy;
  }
}
