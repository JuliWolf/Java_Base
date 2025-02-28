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
@Table(name = "relation_type", schema = "public")
public class RelationType {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "relation_type_id", updatable = false, nullable = false)
  private UUID relationTypeId;

  @Column(name = "relation_type_name")
  private String relationTypeName;

  @Column(name = "relation_type_description")
  private String relationTypeDescription;

  @Column(name = "component_number")
  private Integer componentNumber;

  @Column(name = "responsibility_inheritance_flag")
  private Boolean responsibilityInheritanceFlag = false;

  @Column(name = "hierarchy_flag")
  private Boolean hierarchyFlag = false;

  @Column(name = "uniqueness_flag", columnDefinition = "boolean default true")
  private Boolean uniquenessFlag = true;

  @Column(name = "self_related_flag", columnDefinition = "boolean default false")
  private Boolean selfRelatedFlag = false;

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

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public String getLanguageName () {
    return language != null ? language.getLanguage() : null;
  }

  public RelationType (
    String relationTypeName,
    String relationTypeDescription,
    Integer componentNumber,
    Boolean responsibilityInheritanceFlag,
    Boolean hierarchyFlag,
    Boolean uniquenessFlag,
    Boolean selfRelatedFlag,
    Language language,
    User createdBy
  ) {
    this.relationTypeName = relationTypeName;
    this.relationTypeDescription = relationTypeDescription;
    this.componentNumber = componentNumber;
    this.responsibilityInheritanceFlag = responsibilityInheritanceFlag;
    this.hierarchyFlag = hierarchyFlag;
    this.uniquenessFlag = uniquenessFlag;
    this.selfRelatedFlag = selfRelatedFlag;
    this.language = language;
    this.createdBy = createdBy;
  }

  public RelationType (
    String relationTypeName,
    String relationTypeDescription,
    Integer componentNumber,
    Boolean responsibilityInheritanceFlag,
    Boolean hierarchyFlag,
    Language language,
    User createdBy
  ) {
    this.relationTypeName = relationTypeName;
    this.relationTypeDescription = relationTypeDescription;
    this.componentNumber = componentNumber;
    this.responsibilityInheritanceFlag = responsibilityInheritanceFlag;
    this.hierarchyFlag = hierarchyFlag;
    this.uniquenessFlag = true;
    this.language = language;
    this.createdBy = createdBy;
  }
}
