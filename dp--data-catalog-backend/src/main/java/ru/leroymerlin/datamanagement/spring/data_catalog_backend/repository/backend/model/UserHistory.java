package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils.HistoryDateUtils;

/**
 * @author juliwolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(
  name = "user_history", schema = "public",
  indexes = @Index(name = "idx_user_id_valid_from_valid_to", columnList = "user_id, valid_from, valid_to")
)
public class UserHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_history_id", updatable = false, nullable = false)
  private UUID userHistoryId;

  @Column(name="user_id")
  private UUID userId;

  @Column(name = "boss_k_pid")
  private String bossKPid;

  @Column(name = "username", updatable = false, nullable = false, columnDefinition = "varchar(30)")
  private String username;

  @Column(name = "email", columnDefinition = "varchar(60)")
  private String email;

  @Column(name = "first_name", columnDefinition = "varchar(255)")
  private String firstName;

  @Column(name = "last_name", columnDefinition = "varchar(255)")
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", columnDefinition = "varchar(10)")
  private SourceType source;

  @Column(name = "messenger", columnDefinition = "varchar(255)")
  private String messenger;

  @Column(name = "struct_unit_id")
  private UUID structUnitId;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", columnDefinition = "varchar(8)")
  private UserType userType;

  @Column(name = "user_photo_link", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String userPhotoLink;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_work_status", columnDefinition = "varchar(8)")
  private UserWorkStatus userWorkStatus;

  @Column(name="source_language")
  private UUID languageId;

  @Column(name = "created_on")
  private java.sql.Timestamp createdOn;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "last_modified_on")
  private java.sql.Timestamp lastModifiedOn;

  @Column(name = "last_modified_by")
  private UUID lastModifiedBy;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private java.sql.Timestamp deletedOn;

  @Column(name = "deleted_by")
  private UUID deletedBy;

  @Column(name = "valid_from")
  private java.sql.Timestamp validFrom;

  @Column(name = "valid_to")
  private java.sql.Timestamp validTo;

  public UserHistory (User user) {
    this.userId = user.getUserId();
    this.bossKPid = user.getBossKPid();
    this.username = user.getUsername();
    this.email = user.getEmail();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.source = user.getSource();
    this.messenger = user.getMessenger();
    this.structUnitId = user.getStructUnit() != null ? user.getStructUnit().getStructUnitId() : null;;
    this.userType = user.getUserType();
    this.userPhotoLink = user.getUserPhotoLink();
    this.userWorkStatus = user.getUserWorkStatus();
    this.languageId = user.getLanguage() != null ? user.getLanguage().getLanguageId() : null;
    this.createdOn = user.getCreatedOn();
    this.createdBy = user.getCreatedByUUID();
    this.lastModifiedOn = user.getLastModifiedOn();
    this.lastModifiedBy = user.getLastModifiedByUUID();
    this.isDeleted = user.getIsDeleted();
    this.deletedOn = user.getDeletedOn();
    this.deletedBy = user.getDeletedByUUID();
  }

  public void setCreatedValidDate () {
    this.validFrom = this.createdOn;
    this.validTo = HistoryDateUtils.getValidToDefaultTime();
  }

  public void setUpdatedValidDate () {
    this.validFrom = this.lastModifiedOn;
    this.validTo = HistoryDateUtils.getValidToDefaultTime();
  }

  public void setDeletedValidDate () {
    this.validFrom = this.deletedOn;
    this.validTo = this.deletedOn;
  }
}
