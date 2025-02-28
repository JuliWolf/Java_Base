package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.List;
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
@Table(name = "role", schema = "public")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "role_id", updatable = false, nullable = false)
  private UUID roleId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_language", referencedColumnName = "language_id")
  @ToString.Exclude
  private Language language;

  // is unique if other roles have deleted_flag=false
  @Column(name = "role_name")
  private String roleName;

  @Column(name = "role_description")
  private String roleDescription;

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

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "role_id", referencedColumnName = "role_id")
  @ToString.Exclude
  private List<Responsibility> responsibilities;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "role_id", referencedColumnName = "role_id")
  @ToString.Exclude
  private List<GlobalResponsibility> globalResponsibilities;

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public String getLanguageName () {
    return language != null ? language.getLanguage() : null;
  }

  public Role(String roleName, String roleDescription, Language language, User createdBy) {
    this.roleName = roleName;
    this.roleDescription = roleDescription;
    this.language = language;
    this.createdBy = createdBy;
  }
}
