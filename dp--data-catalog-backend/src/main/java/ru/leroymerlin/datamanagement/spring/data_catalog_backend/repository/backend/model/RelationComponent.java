package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name= "relation_component", schema = "public")
public class RelationComponent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name="relation_component_id", updatable = false, nullable = false)
  private UUID relationComponentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_id", referencedColumnName = "relation_id")
  @ToString.Exclude
  private Relation relation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_type_component_id", referencedColumnName = "relation_type_component_id")
  @ToString.Exclude
  private RelationTypeComponent relationTypeComponent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_id", referencedColumnName = "asset_id")
  @ToString.Exclude
  private Asset asset;

  @Enumerated(EnumType.STRING)
  @Column(name = "hierarchy_role")
  private HierarchyRole hierarchyRole;

  @Enumerated(EnumType.STRING)
  @Column(name = "responsibility_inheritance_role")
  private ResponsibilityInheritanceRole responsibilityInheritanceRole;

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

  public RelationComponent (Relation relation, RelationTypeComponent relationTypeComponent, Asset asset, HierarchyRole hierarchyRole, ResponsibilityInheritanceRole responsibilityInheritanceRole, User createdBy) {
    this.relation = relation;
    this.relationTypeComponent = relationTypeComponent;
    this.asset = asset;
    this.hierarchyRole = hierarchyRole;
    this.responsibilityInheritanceRole = responsibilityInheritanceRole;
    this.createdBy = createdBy;
  }
}
