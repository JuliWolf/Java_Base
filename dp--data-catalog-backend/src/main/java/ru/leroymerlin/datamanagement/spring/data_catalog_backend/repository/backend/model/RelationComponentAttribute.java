package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.sql.Timestamp;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.models.AttributeTypeKind;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "relation_component_attribute", schema = "public")
public class RelationComponentAttribute implements AttributeTypeKind {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "relation_component_attribute_id", updatable = false, nullable = false)
  private UUID relationComponentAttributeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_type_id", referencedColumnName = "attribute_type_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  @ToString.Exclude
  private AttributeType attributeType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_component_id", referencedColumnName = "relation_component_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  @ToString.Exclude
  private RelationComponent relationComponent;

  @Column(name = "value", columnDefinition = "text")
  private String value;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_language", referencedColumnName = "language_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @ToString.Exclude
  private Language language;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "user_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @ToString.Exclude
  private User createdBy;

  @Column(name = "last_modified_on")
  private java.sql.Timestamp lastModifiedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_modified_by", referencedColumnName = "user_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @ToString.Exclude
  private User modifiedBy;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private java.sql.Timestamp deletedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by", referencedColumnName = "user_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
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

  public RelationComponentAttribute (AttributeType attributeType, RelationComponent relationComponent, Language language, User createdBy) {
    this.attributeType = attributeType;
    this.relationComponent = relationComponent;
    this.language = language;
    this.createdBy = createdBy;
    this.createdOn = new Timestamp(System.currentTimeMillis());
  }
}
