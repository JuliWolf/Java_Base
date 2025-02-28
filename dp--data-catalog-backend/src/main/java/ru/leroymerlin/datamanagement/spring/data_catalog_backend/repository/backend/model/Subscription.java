package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
@Table(name = "subscrption", schema = "public")
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "subscription_id", updatable = false, nullable = false)
  private UUID subscriptionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_user_id", referencedColumnName = "user_id")
  @ToString.Exclude
  private User ownerUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_id", referencedColumnName = "asset_id")
  @ToString.Exclude
  private Asset asset;

  @Column(name="notification_schedule", columnDefinition = "varchar(20)")
  @Size(max = 20)
  private String notificationSchedule;

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

  public Subscription (User ownerUser, Asset asset, String notificationSchedule, User createdBy) {
    this.ownerUser = ownerUser;
    this.asset = asset;
    this.notificationSchedule = notificationSchedule;
    this.createdBy = createdBy;
  }
}
