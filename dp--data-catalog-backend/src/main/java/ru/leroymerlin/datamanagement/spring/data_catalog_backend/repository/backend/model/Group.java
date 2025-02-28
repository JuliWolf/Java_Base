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
@Table(name = "group", schema = "public")
public class Group {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "group_id", updatable = false, nullable = false)
  private UUID groupId;

  @Column(name = "group_name")
  private String groupName;

  @Column(name = "group_description")
  private String groupDescription;

  @Column(name = "group_email")
  private String groupEmail;

  @Column(name = "group_messenger")
  private String groupMessenger;

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

  public Group(String groupName, String groupDescription, String groupEmail, String groupMessenger, User createdBy) {
    this.groupName = groupName;
    this.groupDescription = groupDescription;
    this.groupEmail = groupEmail;
    this.groupMessenger = groupMessenger;
    this.createdBy = createdBy;
  }
}
