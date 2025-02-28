package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "attribute_type", schema = "public")
public class AttributeType {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "attribute_type_id", updatable = false, nullable = false)
  private UUID attributeTypeId;

  @Column(name = "attribute_type_name")
  private String attributeTypeName;

  @Column(name = "attribute_type_description")
  private String attributeTypeDescription;

  @Enumerated(EnumType.STRING)
  @Column(name = "attribute_kind")
  private AttributeKindType attributeKindType;

  @Column(name = "validation_mask")
  private String validationMask;

  @Column(name = "rdm_table_id")
  private String rdmTableId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_language", referencedColumnName = "language_id")
  @ToString.Exclude
  private Language language;

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

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_type_id", referencedColumnName = "attribute_type_id")
  @ToString.Exclude
  private List<AttributeTypeAllowedValue> allowedValues;

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public String getLanguageName () {
    return language != null ? language.getLanguage() : null;
  }

  public AttributeType (String attributeTypeName, String attributeTypeDescription, AttributeKindType attributeKindType, String validationMask, String rdmTableId, Language language, User createdBy) {
    this.attributeTypeName = attributeTypeName;
    this.attributeTypeDescription = attributeTypeDescription;
    this.attributeKindType = attributeKindType;
    this.validationMask = validationMask;
    this.rdmTableId = rdmTableId;
    this.language = language;
    this.createdBy = createdBy;
  }
}
