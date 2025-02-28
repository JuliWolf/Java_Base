package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.models.AttributeTypeKind;
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
  name = "attribute_history", schema = "public",
  indexes = @Index(name = "idx_attribute_asset_id_valid_from_valid_to", columnList = "asset_id, valid_from, valid_to")
)
public class AttributeHistory implements AttributeTypeKind {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "attribute_history_id", updatable = false, nullable = false)
  private UUID attributeHistoryId;

  @Column(name = "attribute_id")
  private UUID attributeId;

  @Column(name = "attribute_type_id")
  private UUID attributeTypeId;

  @Column(name = "asset_id")
  private UUID assetId;

  @Column(name = "value", columnDefinition = "text")
  private String value;

  @Column(name = "integer_flag")
  private Boolean isInteger;

  @Column(name = "value_numeric")
  private Double valueNumeric;

  @Column(name = "value_bool")
  private Boolean valueBoolean;

  @Column(name = "value_datetime")
  private java.sql.Timestamp valueDatetime;

  @Column(name = "source_language")
  private UUID languageId;

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

  public AttributeHistory (Attribute attribute) {
    this.attributeId = attribute.getAttributeId();
    this.attributeTypeId = attribute.getAttributeType().getAttributeTypeId();
    this.assetId = attribute.getAsset().getAssetId();
    this.value = attribute.getValue();
    this.isInteger = attribute.getIsInteger();
    this.valueNumeric = attribute.getValueNumeric();
    this.valueBoolean = attribute.getValueBoolean();
    this.valueDatetime = attribute.getValueDatetime();
    this.languageId = attribute.getLanguage() != null ? attribute.getLanguage().getLanguageId() : null;
    this.createdOn = attribute.getCreatedOn();
    this.createdBy = attribute.getCreatedByUUID();
    this.lastModifiedOn = attribute.getLastModifiedOn();
    this.lastModifiedBy = attribute.getLastModifiedByUUID();
    this.isDeleted = attribute.getIsDeleted();
    this.deletedOn = attribute.getDeletedOn();
    this.deletedBy = attribute.getDeletedByUUID();
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
