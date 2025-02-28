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
@Table(name = "attribute_type_allowed_value", schema = "public")
public class AttributeTypeAllowedValue {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "value_id", updatable = false, nullable = false)
  private UUID valueId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_type_id", referencedColumnName = "attribute_type_id")
  @ToString.Exclude
  private AttributeType attributeType;

  @Column(name = "value")
  private String value;

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

  public AttributeTypeAllowedValue (AttributeType attributeType, String value, Language language, User createdBy) {
    this.attributeType = attributeType;
    this.value = value;
    this.language = language;
    this.createdBy = createdBy;
  }
}
