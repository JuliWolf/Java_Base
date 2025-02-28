package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.sql.Timestamp;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name= "responsibility", schema = "public")
public class Responsibility {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name="responsibility_id", updatable = false, nullable = false)
  private UUID responsibilityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = true)
  @ToString.Exclude
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", referencedColumnName = "group_id", nullable = true)
  @ToString.Exclude
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_id", referencedColumnName = "asset_id", nullable = false)
  @ToString.Exclude
  private Asset asset;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
  @ToString.Exclude
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "responsible_type")
  private ResponsibleType responsibleType;

  @Column(name = "inherited_flag")
  private Boolean inheritedFlag = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_responsibility_id", referencedColumnName = "responsibility_id")
  @ToString.Exclude
  private Responsibility parentResponsibility;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relation_id", referencedColumnName = "relation_id")
  @ToString.Exclude
  private Relation relation;

  @Column(name = "created_on")
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

  public Responsibility (User user, Group group, Asset asset, Role role, ResponsibleType responsibleType, User createdBy) {
    this.user = user;
    this.group = group;
    this.asset = asset;
    this.role = role;
    this.responsibleType = responsibleType;
    this.createdOn = new Timestamp(System.currentTimeMillis());
    this.createdBy = createdBy;
  }
}
