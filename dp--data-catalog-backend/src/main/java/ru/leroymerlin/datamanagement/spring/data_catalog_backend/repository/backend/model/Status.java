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
@Table(name = "status", schema = "public")
public class Status {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "status_id", updatable = false, nullable = false)
  private UUID statusId;

  @Column(name = "status_name")
  private String statusName;

  @Column(name = "status_description")
  private String statusDescription;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="source_language", referencedColumnName = "language_id")
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

  public Status (String statusName, String statusDescription, Language language, User createdBy) {
    this.statusName = statusName;
    this.statusDescription = statusDescription;
    this.language = language;
    this.createdBy = createdBy;
  }
}
