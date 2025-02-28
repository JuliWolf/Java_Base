package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.sql.Timestamp;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * @author JuliWolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name= "struct_unit", schema = "public")
public class StructUnit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name="struct_unit_id", updatable = false, nullable = false)
  private UUID structUnitId;

  @Column(name = "struct_unit_source_system_id", columnDefinition = "int(4)")
  private String structUnitSourceSystemId;

  @Column(name = "struct_unit_name", columnDefinition = "varchar(255)")
  private String structUnitName;

  @Column(name = "struct_unit_level", columnDefinition = "int(4)")
  private Integer structUnitLevel;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @JoinColumn(name = "parent_struct_unit_id", referencedColumnName = "struct_unit_id")
  @ToString.Exclude
  private StructUnit parentStructUnit;

  @Column(name = "struct_unit_source_firm_id", columnDefinition = "int(4)")
  private Integer structUnitSourceFirmId;

  @Column(name = "created_on")
  @CreationTimestamp
  private Timestamp createdOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User createdBy;

  @Column(name = "last_modified_on")
  private Timestamp lastModifiedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_modified_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User modifiedBy;

  @Column(name = "last_login_datetime")
  private Timestamp lastLoginTime;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private Timestamp deletedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User deletedBy;

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public StructUnit (String structUnitName, Integer structUnitLevel, Integer structUnitSourceFirmId, User createdBy) {
    this.structUnitName = structUnitName;
    this.structUnitLevel = structUnitLevel;
    this.structUnitSourceFirmId = structUnitSourceFirmId;
    this.createdBy = createdBy;
  }
}
