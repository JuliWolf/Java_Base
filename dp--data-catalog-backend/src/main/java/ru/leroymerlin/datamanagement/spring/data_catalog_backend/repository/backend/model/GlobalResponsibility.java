package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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
@Table(name= "global_responsibility", schema = "public")
public class GlobalResponsibility {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name="global_responsibility_id", updatable = false, nullable = false)
  private UUID globalResponsibilityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  @ToString.Exclude
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", referencedColumnName = "group_id")
  @ToString.Exclude
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", referencedColumnName = "role_id")
  @ToString.Exclude
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "responsible_type")
  private ResponsibleType responsibleType;

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

  public UUID getResponsibleId () {
    return switch (getResponsibleType()) {
      case USER -> getUser().getUserId();
      case GROUP -> getGroup().getGroupId();
    };
  }

  public String getResponsibleName () {
    return switch (getResponsibleType()) {
      case USER -> getUser().getUsername();
      case GROUP -> getGroup().getGroupName();
    };
  }

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public GlobalResponsibility(User user, Group group, Role role, ResponsibleType responsibleType, User createdBy) {
    this.user = user;
    this.group = group;
    this.role = role;
    this.responsibleType = responsibleType;
    this.createdBy = createdBy;
  }
}
