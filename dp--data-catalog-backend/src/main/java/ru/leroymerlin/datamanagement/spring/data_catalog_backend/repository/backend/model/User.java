package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.sql.Timestamp;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;

/**
 * @author JuliWolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name= "user", schema = "public")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name="user_id", updatable = false, nullable = false)
  private UUID userId;

  @Column(name = "boss_k_pid")
  private String bossKPid;

  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "email")
  private String email;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(name = "source")
  private SourceType source;

  @Column(name = "messenger")
  private String messenger;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="struct_unit_id", referencedColumnName = "struct_unit_id")
  @ToString.Exclude
  private StructUnit structUnit;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", columnDefinition = "varchar(8)")
  private UserType userType;

  @Column(name = "user_photo_link", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String userPhotoLink;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_work_status", columnDefinition = "varchar(8)")
  private UserWorkStatus userWorkStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="language_id", referencedColumnName = "language_id")
  @ToString.Exclude
  private Language language;

  @Column(name = "created_on")
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

  @Column(name = "last_login_datetime")
  private java.sql.Timestamp lastLoginTime;

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

  public UUID getLastModifiedByUUID () {
    return modifiedBy != null ? modifiedBy.getUserId() : null;
  }

  public UUID getDeletedByUUID () {
    return deletedBy != null ? deletedBy.getUserId() : null;
  }

  public String getLanguageName () {
    return language != null ? language.getLanguage() : null;
  }

  public User(String username, String firstName, String lastName, SourceType source, String userEmail) {
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.source = source;
    this.email = userEmail;
    this.createdOn = new Timestamp(System.currentTimeMillis());
    this.lastLoginTime = new Timestamp(System.currentTimeMillis());
  }

  public User (
    String username,
    String email,
    String firstName,
    String lastName,
    SourceType source,
    StructUnit structUnit,
    UserType userType,
    UserWorkStatus userWorkStatus,
    String userPhotoLink,
    Language language,
    User createdBy
  ) {
    this.username = username;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.source = source;
    this.structUnit = structUnit;
    this.userType = userType;
    this.userWorkStatus = userWorkStatus;
    this.userPhotoLink = userPhotoLink;
    this.language = language;
    this.createdOn = new Timestamp(System.currentTimeMillis());
    this.createdBy = createdBy;
  }
}
