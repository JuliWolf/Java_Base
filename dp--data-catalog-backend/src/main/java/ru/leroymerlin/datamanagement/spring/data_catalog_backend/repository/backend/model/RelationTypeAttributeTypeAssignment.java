package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "relation_type_attribute_type_assignment", schema = "public")
public class RelationTypeAttributeTypeAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "relation_type_attribute_type_assignment_id", updatable = false, nullable = false)
  private UUID relationTypeAttributeTypeAssignmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_type_id", referencedColumnName = "relation_type_id")
  @ToString.Exclude
  private RelationType relationType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_type_id", referencedColumnName = "attribute_type_id")
  @ToString.Exclude
  private AttributeType attributeType;

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

  public RelationTypeAttributeTypeAssignment (RelationType relationType, AttributeType attributeType, User createdBy) {
    this.relationType = relationType;
    this.attributeType = attributeType;
    this.createdBy = createdBy;
  }
}
