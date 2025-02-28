package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "relation_type_component", schema = "public")
public class RelationTypeComponent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "relation_type_component_id", updatable = false, nullable = false)
  private UUID relationTypeComponentId;

  @Column(name = "relation_type_component_name")
  private String relationTypeComponentName;

  @Column(name = "relation_type_component_description")
  private String relationTypeComponentDescription;

  @Enumerated(EnumType.STRING)
  @Column(name = "responsibility_inheritance_role")
  private ResponsibilityInheritanceRole responsibilityInheritanceRole;

  @Enumerated(EnumType.STRING)
  @Column(name = "hierarchy_role")
  private HierarchyRole hierarchyRole;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_language", referencedColumnName = "language_id")
  @ToString.Exclude
  private Language language;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_type_id", referencedColumnName = "relation_type_id")
  @ToString.Exclude
  private RelationType relationType;

  @Column(name="single_relation_type_component_for_asset_flag", columnDefinition = "boolean default false")
  private Boolean singleRelationTypeComponentForAssetFlag = false;

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

  public RelationTypeComponent (
    String relationTypeComponentName,
    String relationTypeComponentDescription,
    ResponsibilityInheritanceRole responsibilityInheritanceRole,
    HierarchyRole hierarchyRole,
    Boolean singleRelationTypeComponentForAssetFlag,
    Language language,
    RelationType relationType,
    User createdBy
  ) {
    this.relationTypeComponentName = relationTypeComponentName;
    this.relationTypeComponentDescription = relationTypeComponentDescription;
    this.responsibilityInheritanceRole = responsibilityInheritanceRole;
    this.hierarchyRole = hierarchyRole;
    this.singleRelationTypeComponentForAssetFlag = singleRelationTypeComponentForAssetFlag;
    this.language = language;
    this.relationType = relationType;
    this.createdBy = createdBy;
  }
}
