package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "asset_type_card_header_assignment", schema = "public")
public class AssetTypeCardHeaderAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "asset_type_card_header_assignment_id", updatable = false, nullable = false)
  private UUID assetTypeCardHeaderAssignmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.CASCADE)
  private AssetType assetType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "description_field_attribute_type_id", referencedColumnName = "attribute_type_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.CASCADE)
  private AttributeType descriptionFieldAttributeType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_field_role_id", referencedColumnName = "role_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Role ownerFieldRole;

  @Column(name = "created_on")
  @CreationTimestamp
  private java.sql.Timestamp createdOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "user_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User createdBy;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private java.sql.Timestamp deletedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by", referencedColumnName = "user_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User deletedBy;

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public UUID getDescriptionFieldAttributeTypeUUID () {
    return descriptionFieldAttributeType != null ? descriptionFieldAttributeType.getAttributeTypeId() : null;
  }

  public UUID getOwnerFieldRoleUUID () {
    return ownerFieldRole != null ? ownerFieldRole.getRoleId() : null;
  }

  public AssetTypeCardHeaderAssignment (AssetType assetType, AttributeType descriptionFieldAttributeType, Role ownerFieldRole, User createdBy) {
    this.assetType = assetType;
    this.descriptionFieldAttributeType = descriptionFieldAttributeType;
    this.ownerFieldRole = ownerFieldRole;
    this.createdBy = createdBy;
  }
}
