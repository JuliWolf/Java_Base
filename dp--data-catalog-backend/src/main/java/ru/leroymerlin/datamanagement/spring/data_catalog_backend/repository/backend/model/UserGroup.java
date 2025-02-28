package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.sql.Timestamp;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "user_group", schema = "public")
public class UserGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_group_id", updatable = false, nullable = false)
  private UUID userGroupId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  @ToString.Exclude
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", referencedColumnName = "group_id")
  @ToString.Exclude
  private Group group;

  @Column(name = "added_on")
  private java.sql.Timestamp addedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "added_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User addedBy;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private java.sql.Timestamp deletedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User deletedBy;

  public UUID getAddedByUUID () {
    return addedBy != null ? addedBy.getUserId() : null;
  }

  public UserGroup (User user, Group group, User addedBy) {
    this.user = user;
    this.group = group;
    this.addedOn = new Timestamp(System.currentTimeMillis());
    this.addedBy = addedBy;
  }
}
