package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.models.AttributeTypeKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(
  name = "relation_component_attribute_history", schema = "public",
  indexes = @Index(name = "idx_relation_component_id_valid_from_valid_to", columnList = "relation_component_id, valid_from, valid_to")
)
public class RelationComponentAttributeHistory implements AttributeTypeKind {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "relation_component_attribute_history_id", updatable = false, nullable = false)
  private UUID relationComponentAttributeHistoryId;

  @Column(name = "relation_component_attribute_id")
  private UUID relationComponentAttributeId;

  @Column(name = "attribute_type_id")
  private UUID attributeTypeId;

  @Column(name = "relation_component_id")
  private UUID relationComponentId;

  @Column(name = "value", columnDefinition = "text")
  private String value;

  @Column(name = "source_language")
  private UUID languageId;

  @Column(name = "integer_flag")
  private Boolean isInteger;

  @Column(name = "value_numeric")
  private Double valueNumeric;

  @Column(name = "value_bool")
  private Boolean valueBoolean;

  @Column(name = "value_datetime")
  private java.sql.Timestamp valueDatetime;

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

  public RelationComponentAttributeHistory (RelationComponentAttribute relationComponentAttribute) {
    this.relationComponentAttributeId = relationComponentAttribute.getRelationComponentAttributeId();
    this.attributeTypeId = relationComponentAttribute.getAttributeType().getAttributeTypeId();
    this.relationComponentId = relationComponentAttribute.getRelationComponent().getRelationComponentId();
    this.value = relationComponentAttribute.getValue();
    this.isInteger = relationComponentAttribute.getIsInteger();
    this.valueNumeric = relationComponentAttribute.getValueNumeric();
    this.valueBoolean = relationComponentAttribute.getValueBoolean();
    this.valueDatetime = relationComponentAttribute.getValueDatetime();
    this.languageId = relationComponentAttribute.getLanguage() != null ? relationComponentAttribute.getLanguage().getLanguageId() : null;
    this.createdOn = relationComponentAttribute.getCreatedOn();
    this.createdBy = relationComponentAttribute.getCreatedByUUID();
    this.lastModifiedOn = relationComponentAttribute.getLastModifiedOn();
    this.lastModifiedBy = relationComponentAttribute.getLastModifiedByUUID();
    this.isDeleted = relationComponentAttribute.getIsDeleted();
    this.deletedOn = relationComponentAttribute.getDeletedOn();
    this.deletedBy = relationComponentAttribute.getDeletedByUUID();
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
